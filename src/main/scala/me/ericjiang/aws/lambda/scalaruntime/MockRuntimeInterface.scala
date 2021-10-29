package me.ericjiang.aws.lambda.scalaruntime
import me.ericjiang.aws.lambda.scalaruntime.model.{ErrorRequest, Invocation, InvocationHeaders, StatusResponse}

import scala.collection.mutable
import scala.util.{Success, Try}

object MockRuntimeInterface extends RuntimeInterface {

  private val invocations = mutable.Queue(Invocation(
    payload = """{"name": "Eric"}""",
    headers = InvocationHeaders(
      awsRequestId = "123",
      deadlineMs = System.currentTimeMillis() + 10000,
      invokedFunctionArn = "",
      traceId = "",
      clientContext = "",
      cognitoIdentity = "")))

  override def getNextInvocation: Try[Invocation] = Success(invocations.dequeue)

  override def postInvocationResponse(awsRequestId: String, response: String): Try[StatusResponse] = {
    println(s"Received invocation response for request $awsRequestId: $response")
    Success(StatusResponse("success"))
  }

  override def postInitializationError(errorRequest: ErrorRequest): Try[StatusResponse] = {
    println(s"Received initialization error: $errorRequest")
    Success(StatusResponse("success"))
  }

  override def postInvocationError(awsRequestId: String, errorRequest: ErrorRequest): Try[StatusResponse] = {
    println(s"Received invocation error for request $awsRequestId: $errorRequest")
    Success(StatusResponse("success"))
  }
}
