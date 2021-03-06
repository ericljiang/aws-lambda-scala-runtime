package me.ericjiang.aws.lambda.scalaruntime.runtimeinterface

import com.typesafe.scalalogging.LazyLogging
import me.ericjiang.aws.lambda.scalaruntime.runtimeinterface.model.{ErrorRequest, Invocation, StatusResponse}

import java.util.concurrent.LinkedBlockingQueue
import scala.util.{Success, Try}

object MockRuntimeInterface extends RuntimeInterface with LazyLogging {

  val invocations = new LinkedBlockingQueue[Invocation]

  override def getNextInvocation: Try[Invocation] = Try(invocations.take())

  override def postInvocationResponse(awsRequestId: String, response: String): Try[StatusResponse] = {
    logger.info(s"Received invocation response for request $awsRequestId:\n$response")
    Success(StatusResponse("success"))
  }

  override def postInitializationError(errorRequest: ErrorRequest): Try[StatusResponse] = {
    logger.info(s"Received initialization error:\n$errorRequest")
    Success(StatusResponse("success"))
  }

  override def postInvocationError(awsRequestId: String, errorRequest: ErrorRequest): Try[StatusResponse] = {
    logger.info(s"Received invocation error for request $awsRequestId:\n$errorRequest")
    Success(StatusResponse("success"))
  }
}
