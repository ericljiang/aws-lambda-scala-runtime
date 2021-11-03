package me.ericjiang.aws.lambda.scalaruntime

import com.typesafe.scalalogging.LazyLogging
import io.circe.parser.decode
import io.circe.syntax._
import me.ericjiang.aws.lambda.scalaruntime.runtimeinterface.model.{ErrorRequest, Invocation, RuntimeInterfaceError, StatusResponse}
import me.ericjiang.aws.lambda.scalaruntime.runtimeinterface.{DefaultRuntimeInterface, RuntimeInterface}

import scala.annotation.tailrec
import scala.language.implicitConversions
import scala.util.chaining.scalaUtilChainingOps
import scala.util.{Failure, Success, Try}

class LambdaRuntime(runtimeInterface: RuntimeInterface = new DefaultRuntimeInterface) extends LazyLogging {

  @throws[LambdaRuntimeFailure]("if an unrecoverable exception occurs")
  def run[I, O](handler: I => O)(implicit decoder: io.circe.Decoder[I], encoder: io.circe.Encoder[O]): Nothing = {
    def handlerWithSerialization(input: String): String = decode[I](input).toTry
      .recover(exception => throw new RuntimeException(s"Error deserializing input '$input'", exception))
      .map(handler(_))
      .map(_.asJson.noSpaces)
      .get
    run(handlerWithSerialization _)
  }

  @tailrec
  @throws[LambdaRuntimeFailure]("if an unrecoverable exception occurs")
  final def run(handler: String => String): Nothing = {
    Try(runtimeInterface.getNextInvocation).flatten
      .flatMap(handleInvocation(_, handler))
      .tap {
        case Failure(exception) =>
          logger.error("Exiting due to unhandled exception.", exception)
          exception match {
            case RuntimeInterfaceError(_, 500, _) => // exit immediately
            case exception => reportInitializationError(exception)
          }
        case _ =>
      }
      .recover(exception => throw new LambdaRuntimeFailure(exception))
      .get
    run(handler)
  }

  private def handleInvocation(invocation: Invocation, handler: String => String): Try[StatusResponse] =
    Try(handler(invocation.payload))
      .flatMap(runtimeInterface.postInvocationResponse(invocation.headers.awsRequestId, _))
      .recoverWith {
        // runtime API specifies not to recover from container errors
        case exception @ RuntimeInterfaceError(_, 500, _) =>
          logger.error("Encountered container failure from the runtime interface.", exception)
          throw exception
        // attempt to report all other errors back to the runtime interface and continue
        case exception: Exception =>
          logger.error("Encountered exception during invocation. Reporting it to the runtime interface.", exception)
          runtimeInterface.postInvocationError(invocation.headers.awsRequestId, ErrorRequest(exception))
      }

  private def reportInitializationError(error: Throwable): Unit =
    runtimeInterface.postInitializationError(ErrorRequest(error)) match {
      case Success(_) =>
        logger.info("Successfully reported unhandled exception to the runtime interface.")
      case Failure(reportingException) =>
        logger.error("Failed to report unhandled exception to the runtime interface.", reportingException)
    }
}
