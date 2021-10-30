package me.ericjiang.aws.lambda.scalaruntime.runtimeinterface

import me.ericjiang.aws.lambda.scalaruntime.runtimeinterface.model.{ErrorRequest, Invocation, StatusResponse}

import scala.util.Try

/**
 * AWS Lambda provides an HTTP API for custom runtimes to receive invocation events from Lambda and send response data
 * back within the Lambda execution environment.
 *
 * The OpenAPI specification for the runtime API version 2018-06-01 is available in
 * [[https://docs.aws.amazon.com/lambda/latest/dg/samples/runtime-api.zip]]
 *
 * Reference: [[https://docs.aws.amazon.com/lambda/latest/dg/runtimes-api.html]]
 */
trait RuntimeInterface {
  def getNextInvocation: Try[Invocation]

  def postInvocationResponse(awsRequestId: String, response: String): Try[StatusResponse]

  def postInitializationError(errorRequest: ErrorRequest): Try[StatusResponse]

  def postInvocationError(awsRequestId: String, errorRequest: ErrorRequest): Try[StatusResponse]
}
