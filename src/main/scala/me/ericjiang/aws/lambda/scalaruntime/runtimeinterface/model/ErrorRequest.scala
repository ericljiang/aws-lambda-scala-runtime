package me.ericjiang.aws.lambda.scalaruntime.runtimeinterface.model

/**
 * Schema for posting errors to the runtime interface.
 *
 * Note from AWS' specification for the runtime API:
 * <blockquote>
 * Runtimes are free to define the format of errors that are reported to the runtime API, however, in order to integrate
 * with other AWS services, runtimes must report all errors using the standard AWS Lambda error format:
 * {{{
 * Content-Type: application/vnd.aws.lambda.error+json:
 * {
 *     "errorMessage": "...",
 *     "errorType": "...",
 *     "stackTrace": [],
 * }
 * }}}
 * </blockquote>
 */
case class ErrorRequest(errorMessage: String, errorType: String, stackTrace: Seq[String])

object ErrorRequest {
  def apply(throwable: Throwable): ErrorRequest = ErrorRequest(
    errorMessage = throwable.getMessage,
    errorType = throwable.getClass.getName,
    stackTrace = throwable.getStackTrace.map(_.toString))
}
