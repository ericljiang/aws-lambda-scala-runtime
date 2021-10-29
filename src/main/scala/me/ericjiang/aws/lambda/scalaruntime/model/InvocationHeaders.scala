package me.ericjiang.aws.lambda.scalaruntime.model

case class InvocationHeaders(
  awsRequestId: String,
  deadlineMs: Long,
  invokedFunctionArn: String,
  traceId: String,
  clientContext: String,
  cognitoIdentity: String
)
