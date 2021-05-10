package sttp.tapir.serverless.aws.examples

import io.circe.syntax._
import sttp.tapir.serverless.aws.sam._

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

object HelloSam extends App {
  val targetFile = "/Users/kubinio/Desktop/workspace/tapir/serverless/aws/examples/template.yaml"

  implicit val samOptions: AwsSamOptions = AwsSamOptions(
    "hello",
    source = CodeSource("java11", "target/jvm-2.13/examples.jar", "sttp.tapir.serverless.aws.examples.HelloHandler::handleRequest")
  )

  val samTemplate = new AwsSamInterpreter().apply(List(HelloHandler.helloEndpoint.endpoint))

  val yaml = Printer(dropNullKeys = true, preserveOrder = true, stringStyle = Printer.StringStyle.Plain)
    .pretty(samTemplate.asJson(AwsSamTemplateEncoders.encoderSamTemplate))

  Files.write(Paths.get(targetFile), yaml.getBytes(StandardCharsets.UTF_8))
}
