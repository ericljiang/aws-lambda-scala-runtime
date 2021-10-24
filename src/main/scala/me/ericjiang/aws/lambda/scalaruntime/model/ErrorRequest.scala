package me.ericjiang.aws.lambda.scalaruntime.model

case class ErrorRequest(errorMessage: String, errorType: String, stackTrace: Seq[String])
