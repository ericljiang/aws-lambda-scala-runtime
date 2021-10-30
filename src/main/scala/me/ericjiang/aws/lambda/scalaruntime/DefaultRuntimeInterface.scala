package me.ericjiang.aws.lambda.scalaruntime

import io.circe.generic.auto._
import me.ericjiang.aws.lambda.scalaruntime.DefaultRuntimeInterface.{DefaultEndpoint, DefaultSttpBackend}
import me.ericjiang.aws.lambda.scalaruntime.exception.RuntimeInterfaceError
import me.ericjiang.aws.lambda.scalaruntime.model.{ErrorRequest, Invocation, StatusResponse}
import sttp.client3._
import sttp.client3.circe._
import sttp.client3.httpclient.HttpClientSyncBackend
import sttp.model.Uri

import scala.concurrent.duration.Duration
import scala.util.Try

class DefaultRuntimeInterface(
  endpoint: String = DefaultEndpoint,
  sttpBackend: SttpBackend[Identity, Any] = DefaultSttpBackend
) extends RuntimeInterface {

  private val apiVersion = "2018-06-01"

  override def getNextInvocation: Try[Invocation] =
    Try(basicRequest
      .get(uri"http://$endpoint/$apiVersion/runtime/invocation/next")
      .readTimeout(Duration.Inf)
      .response(asJson[Invocation])
      .send(sttpBackend)
      .body
      .toTry
    ).flatten
      .recover(wrapHttpErrors("Server error while retrieving next invocation."))

  override def postInvocationResponse(awsRequestId: String, response: String): Try[StatusResponse] =
    post(uri"http://$endpoint/$apiVersion/runtime/invocation/$awsRequestId/response", response)
      .recover(wrapHttpErrors("Server error while posting invocation response."))

  override def postInitializationError(errorRequest: ErrorRequest): Try[StatusResponse] =
    post(uri"http://$endpoint/$apiVersion/runtime/init/error", errorRequest)
      .recover(wrapHttpErrors("Server error while posting initialization error."))

  override def postInvocationError(awsRequestId: String, errorRequest: ErrorRequest): Try[StatusResponse] =
    post(uri"http://$endpoint/$apiVersion/runtime/invocation/$awsRequestId/error", errorRequest)
      .recover(wrapHttpErrors("Server error while posting invocation error."))

  private def post[B: BodySerializer](uri: Uri, body: B): Try[StatusResponse] =Try(basicRequest
    .post(uri)
    .body(body)
    .response(asJson[StatusResponse])
    .send(sttpBackend)
    .body
    .toTry
  ).flatten

  private def wrapHttpErrors(message: String): PartialFunction[Throwable, Nothing] = {
    case HttpError(body: String, statusCode) => throw RuntimeInterfaceError(message, statusCode.code, body)
  }
}

object DefaultRuntimeInterface {
  private lazy val EndpointEnvVar = "AWS_LAMBDA_RUNTIME_API"
  private lazy val DefaultEndpoint = sys.env.getOrElse(EndpointEnvVar,
    throw new IllegalStateException(s"$EndpointEnvVar environment variable not defined."))
  private lazy val DefaultSttpBackend = HttpClientSyncBackend()
}
