package me.ericjiang.aws.lambda.scalaruntime

import io.circe.parser.decode
import io.circe.syntax._

import me.ericjiang.aws.lambda.scalaruntime.exception.{LambdaRuntimeFailure, RuntimeInterfaceError}
import me.ericjiang.aws.lambda.scalaruntime.model.{ErrorRequest, Invocation, StatusResponse}

import scala.annotation.tailrec
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

class LambdaRuntime(runtimeInterface: RuntimeInterface = new DefaultRuntimeInterface) {

  @throws[LambdaRuntimeFailure]("if an unrecoverable exception occurs")
  def run[I, O](handler: I => O)(implicit decoder: io.circe.Decoder[I], encoder: io.circe.Encoder[O]): Nothing = {
    val handlerWithSerialization: String => String = decode[I](_).toTry
      .map(handler(_))
      .map(_.asJson.toString)
      .get
    run(handlerWithSerialization)
  }

  @tailrec
  @throws[LambdaRuntimeFailure]("if an unrecoverable exception occurs")
  final def run(handler: String => String): Nothing = {
    Try(runtimeInterface.getNextInvocation).flatten
      .flatMap(handleInvocation(_, handler))
      .recover { exception =>
        println("Exiting due to unhandled exception.")
        runtimeInterface.postInitializationError(ErrorRequest(exception)) match {
          case Success(_) => println("Successfully reported unhandled exception to the runtime interface.")
          case Failure(_) => println("Failed to report unhandled exception to the runtime interface.")
        }
        throw new LambdaRuntimeFailure(exception)
      }
      .get
    run(handler)
  }

  private def handleInvocation(invocation: Invocation, handler: String => String): Try[StatusResponse] =
    Try(handler(invocation.payload))
      .flatMap(runtimeInterface.postInvocationResponse(invocation.headers.awsRequestId, _))
      .recoverWith {
        // runtime API specifies not to recover from container errors
        case exception: RuntimeInterfaceError if exception.statusCode == 500 =>
          println("Encountered container failure from the runtime interface.")
          throw exception
        // attempt to report all other errors back to the runtime interface and continue
        case exception =>
          println("Encountered exception during invocation. Attempting to report it to the runtime interface.")
          runtimeInterface.postInvocationError(invocation.headers.awsRequestId, ErrorRequest(exception))
      }
}
