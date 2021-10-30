package me.ericjiang.aws.lambda.scalaruntime
import me.ericjiang.aws.lambda.scalaruntime.model.{ErrorRequest, Invocation, StatusResponse}

import java.util.concurrent.LinkedBlockingQueue
import scala.util.{Success, Try}

object MockRuntimeInterface extends RuntimeInterface {

  val invocations = new LinkedBlockingQueue[Invocation]

  override def getNextInvocation: Try[Invocation] = Try(invocations.take())

  override def postInvocationResponse(awsRequestId: String, response: String): Try[StatusResponse] = {
    println(s"Received invocation response for request $awsRequestId:\n$response")
    Success(StatusResponse("success"))
  }

  override def postInitializationError(errorRequest: ErrorRequest): Try[StatusResponse] = {
    println(s"Received initialization error:\n$errorRequest")
    Success(StatusResponse("success"))
  }

  override def postInvocationError(awsRequestId: String, errorRequest: ErrorRequest): Try[StatusResponse] = {
    println(s"Received invocation error for request $awsRequestId:\n$errorRequest")
    Success(StatusResponse("success"))
  }
}
