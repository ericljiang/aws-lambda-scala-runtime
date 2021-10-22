# Scala runtime for AWS Lambda

This is a Scala implementation the layer of code that interacts with the [AWS Lambda runtime API](https://docs.aws.amazon.com/lambda/latest/dg/runtimes-api.html) to receive invocation events, invoke the function handler, and pass function results back to the runtime API.

## Why?
The official Java runtime for AWS Lambda [can already support Scala handlers](https://aws.amazon.com/blogs/compute/writing-aws-lambda-functions-in-scala/); why is a Scala runtime necessary?

This package is intended to be compiled with a Scala function handler into a native image (e.g. with GraalVM), allowing for Scala-based Lambda functions without the cold start times associated with the JVM.

## Features
-

## TODO
-
