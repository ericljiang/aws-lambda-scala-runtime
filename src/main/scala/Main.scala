import io.circe.generic.auto._
import me.ericjiang.aws.lambda.scalaruntime.{LambdaRuntime, MockRuntimeInterface}

object Main extends App {
  println("Hello, World!")
  val i = 1
  val s = i.toString
  s.toInt
  case class Request(name: String)
  case class Response(output: String)
  new LambdaRuntime(MockRuntimeInterface).run((request: Request) => Response(s"Hello, ${request.name}"))
}
