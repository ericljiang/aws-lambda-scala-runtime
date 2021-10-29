package me.ericjiang.aws.lambda.scalaruntime.model

case class Invocation(payload: String, headers: InvocationHeaders)
