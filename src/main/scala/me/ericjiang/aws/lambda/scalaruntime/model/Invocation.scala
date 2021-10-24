package me.ericjiang.aws.lambda.scalaruntime.model

case class Invocation(
  payload: String,
  awsRequestId: String,
  deadlineMs: Long,
  invokedFunctionArn: String,
  traceId: String,
  clientContext: String,
  cognitoIdentity: String
)
