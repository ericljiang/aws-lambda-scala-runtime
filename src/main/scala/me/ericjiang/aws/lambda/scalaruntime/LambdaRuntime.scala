package me.ericjiang.aws.lambda.scalaruntime

import scala.annotation.tailrec

class LambdaRuntime(runtimeInterface: RuntimeInterface) {
  @tailrec
  final def run[I, O](handler: I => O, inputDeserializer: String => I, outputSerializer: O => String): Nothing = {
    val invocation = runtimeInterface.getNextInvocation
    val input = inputDeserializer(invocation.payload)
    val output = handler(input)
    val response = outputSerializer(output)
    runtimeInterface.postInvocationResponse(invocation.awsRequestId, response)
    run(handler, inputDeserializer, outputSerializer)
  }

  def run(handler: String => String): Nothing =
    run(handler, identity[String], identity[String])

  def run[I](handler: I => String, inputDeserializer: String => I): Nothing =
    run(handler, inputDeserializer, identity[String])

  def run[O](handler: String => O, outputSerializer: O => String): Nothing =
    run(handler, identity[String], outputSerializer)
}
