package me.ericjiang.aws.lambda.scalaruntime.exception

class LambdaRuntimeFailure(cause: Throwable) extends Exception(cause)
