package me.ericjiang.aws.lambda.scalaruntime.runtimeinterface.model

case class Invocation(payload: String, headers: InvocationHeaders)
