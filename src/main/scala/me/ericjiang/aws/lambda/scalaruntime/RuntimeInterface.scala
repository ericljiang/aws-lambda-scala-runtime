package me.ericjiang.aws.lambda.scalaruntime

import me.ericjiang.aws.lambda.scalaruntime.model.{ErrorRequest, Invocation}

trait RuntimeInterface {
  def getNextInvocation: Invocation
  def postInvocationResponse(awsRequestId: String, response: String): Unit
  def postInitializationError(errorRequest: ErrorRequest): Unit
  def postInvocationError(awsRequestId: String, errorRequest: ErrorRequest): Unit
}
