import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import me.ericjiang.aws.lambda.scalaruntime.LambdaRuntime
import me.ericjiang.aws.lambda.scalaruntime.runtimeinterface.MockRuntimeInterface
import me.ericjiang.aws.lambda.scalaruntime.runtimeinterface.model.{Invocation, InvocationHeaders}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object Main extends App with LazyLogging {
  println("Hello, World!")
  case class Request(name: String)
  case class Response(output: String)
  Future {
    new LambdaRuntime(MockRuntimeInterface)
      .run((request: Request) => Response(s"Hello, $request"))
  }
  MockRuntimeInterface.invocations.add(Invocation(
    payload = """{"name":"Eric"}""",
    headers = InvocationHeaders(
      awsRequestId = "123",
      deadlineMs = System.currentTimeMillis() + 1.second.toMillis,
      invokedFunctionArn = "",
      traceId = "",
      clientContext = "",
      cognitoIdentity = "")))
  Thread.sleep(1.second.toMillis)
}
