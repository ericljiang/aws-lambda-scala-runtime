package me.ericjiang.aws.lambda.scalaruntime

import me.ericjiang.aws.lambda.scalaruntime.runtimeinterface.RuntimeInterface
import me.ericjiang.aws.lambda.scalaruntime.runtimeinterface.model._
import org.scalatest.flatspec.AnyFlatSpec

import java.util.concurrent.LinkedBlockingQueue
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Success, Try}

class LambdaRuntimeSpec extends AnyFlatSpec {
  "The LambdaRuntime" should "get invocations from and post handler responses to the runtime API" in {
    val invocations = new LinkedBlockingQueue[Invocation]
    val invocationResponses = new ArrayBuffer[String]
    val runtimeInterface = new RuntimeInterface {
      override def getNextInvocation: Try[Invocation] = Try(invocations.take())
      override def postInvocationResponse(awsRequestId: String, response: String): Try[StatusResponse] = {
        invocationResponses += response
        Success(StatusResponse("success"))
      }
      override def postInitializationError(errorRequest: ErrorRequest): Try[StatusResponse] = ???
      override def postInvocationError(awsRequestId: String, errorRequest: ErrorRequest): Try[StatusResponse] = ???
    }
    val runtime = new LambdaRuntime(runtimeInterface)

    invocations.add(Invocation(
      payload = """{"name":"Eric"}""",
      headers = InvocationHeaders(
        awsRequestId = "123",
        deadlineMs = System.currentTimeMillis() + 1.second.toMillis,
        invokedFunctionArn = "",
        traceId = "",
        clientContext = "",
        cognitoIdentity = "")))
    invocations.add(Invocation(
      payload = """{"name":"world"}""",
      headers = InvocationHeaders(
        awsRequestId = "456",
        deadlineMs = System.currentTimeMillis() + 1.second.toMillis,
        invokedFunctionArn = "",
        traceId = "",
        clientContext = "",
        cognitoIdentity = "")))

    case class Request(name: String)
    case class Response(output: String)
    import io.circe.generic.auto._
    val f = Future {
      runtime.run((request: Request) => Response(s"Hello, ${request.name}"))
    }
    Thread.sleep(1.second.toMillis)
    assert(!f.isCompleted)
    assert(invocations.isEmpty)
    assert(invocationResponses.size === 2)
    assert(invocationResponses(0) === """{"output":"Hello, Eric"}""")
    assert(invocationResponses(1) === """{"output":"Hello, world"}""")
  }

  it should "post handler errors to the runtime API" in {
    val invocations = new LinkedBlockingQueue[Invocation]
    var invocationError: Option[ErrorRequest] = None
    val runtimeInterface = new RuntimeInterface {
      override def getNextInvocation: Try[Invocation] = Try(invocations.take())
      override def postInvocationResponse(awsRequestId: String, response: String): Try[StatusResponse] = ???
      override def postInitializationError(errorRequest: ErrorRequest): Try[StatusResponse] = ???
      override def postInvocationError(awsRequestId: String, errorRequest: ErrorRequest): Try[StatusResponse] = {
        invocationError = Some(errorRequest)
        Success(StatusResponse("success"))
      }
    }
    val runtime = new LambdaRuntime(runtimeInterface)

    invocations.add(Invocation(
      payload = """{"name":"Eric"}""",
      headers = InvocationHeaders(
        awsRequestId = "123",
        deadlineMs = System.currentTimeMillis() + 1.second.toMillis,
        invokedFunctionArn = "",
        traceId = "",
        clientContext = "",
        cognitoIdentity = "")))

    case class Request(name: String)
    case class Response(output: String)
    import io.circe.generic.auto._
    def handler(request: Request): Response = throw new Exception("something went wrong")
    Future {
      runtime.run(handler _)
    }
    Thread.sleep(1.second.toMillis)
    assert(invocationError.isDefined)
    assert(invocationError.get.errorMessage === "something went wrong")
    assert(invocationError.get.errorType === "java.lang.Exception")
  }

  it should "post non-handler errors to the runtime API" in {
    var initializationError: Option[ErrorRequest] = None
    val runtimeInterface = new RuntimeInterface {
      override def getNextInvocation: Try[Invocation] = throw new Exception("something went wrong")
      override def postInvocationResponse(awsRequestId: String, response: String): Try[StatusResponse] = ???
      override def postInitializationError(errorRequest: ErrorRequest): Try[StatusResponse] = {
        initializationError = Some(errorRequest)
        Success(StatusResponse("success"))
      }
      override def postInvocationError(awsRequestId: String, errorRequest: ErrorRequest): Try[StatusResponse] = ???
    }
    val runtime = new LambdaRuntime(runtimeInterface)

    case class Request(name: String)
    case class Response(output: String)
    import io.circe.generic.auto._
    Future {
      runtime.run((request: Request) => Response(s"Hello, ${request.name}"))
    }
    Thread.sleep(1.second.toMillis)
    assert(initializationError.isDefined)
    assert(initializationError.get.errorMessage === "something went wrong")
    assert(initializationError.get.errorType === "java.lang.Exception")
  }

  it should "immediately exit on 500-level errors from the runtime API" in {
    val invocations = new LinkedBlockingQueue[Invocation]
    val runtimeInterface = new RuntimeInterface {
      override def getNextInvocation: Try[Invocation] = Try(invocations.take())
      override def postInvocationResponse(awsRequestId: String, response: String): Try[StatusResponse] =
        throw RuntimeInterfaceError("something went wrong", 500, "body")
      override def postInitializationError(errorRequest: ErrorRequest): Try[StatusResponse] = ???
      override def postInvocationError(awsRequestId: String, errorRequest: ErrorRequest): Try[StatusResponse] = ???
    }
    val runtime = new LambdaRuntime(runtimeInterface)

    invocations.add(Invocation(
      payload = """{"name":"Eric"}""",
      headers = InvocationHeaders(
        awsRequestId = "123",
        deadlineMs = System.currentTimeMillis() + 1.second.toMillis,
        invokedFunctionArn = "",
        traceId = "",
        clientContext = "",
        cognitoIdentity = "")))

    case class Request(name: String)
    case class Response(output: String)
    import io.circe.generic.auto._
    assertThrows[LambdaRuntimeFailure] {
      runtime.run((request: Request) => Response(s"Hello, ${request.name}"))
    }
  }
}
