package me.ericjiang.aws.lambda.scalaruntime.runtimeinterface.model

/** Represents errors returned by the runtime interface. */
case class RuntimeInterfaceError(message: String, statusCode: Int, body: String)
  extends Exception(s"$message (status=`$statusCode` body=`$body`)")
