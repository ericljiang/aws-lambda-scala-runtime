import io.circe.generic.auto._
import me.ericjiang.aws.lambda.scalaruntime.model.{Invocation, InvocationHeaders}
import me.ericjiang.aws.lambda.scalaruntime.{LambdaRuntime, MockRuntimeInterface}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object Main extends App {
  println("Hello, World!")
  case class Request(name: String)
  case class Response(output: String)
  val f = Future {
    new LambdaRuntime(MockRuntimeInterface)
      .run((request: Request) => Response(s"Hello, ${request.name}"))
  }
  MockRuntimeInterface.invocations.add(Invocation(
    payload = """{"name": "Eric"}""",
    headers = InvocationHeaders(
      awsRequestId = "123",
      deadlineMs = System.currentTimeMillis() + 1.second.toMillis,
      invokedFunctionArn = "",
      traceId = "",
      clientContext = "",
      cognitoIdentity = "")))
  Thread.sleep(1.second.toMillis)
}
