package sttp.tapir.serverless.aws.lambda

import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import sttp.tapir.Endpoint
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.interceptor.decodefailure.{DecodeFailureHandler, DefaultDecodeFailureHandler}
import sttp.tapir.server.interceptor.exception.DefaultExceptionHandler
import sttp.tapir.server.interceptor.metrics.MetricsRequestInterceptor
import sttp.tapir.server.tests.TestServerInterpreter
import sttp.tapir.tests.Port

import scala.reflect.ClassTag

class AwsLambdaHttpTestServerInterpreter extends TestServerInterpreter[IO, Any, Route[IO], String] {
  override def route[I, E, O](
      e: ServerEndpoint[I, E, O, Any, IO],
      decodeFailureHandler: Option[DecodeFailureHandler],
      metricsInterceptor: Option[MetricsRequestInterceptor[IO, String]]
  ): Route[IO] = {
    implicit val options: AwsServerOptions[IO] = AwsServerOptions.customInterceptors[IO](
      metricsInterceptor = metricsInterceptor,
      exceptionHandler = Some(DefaultExceptionHandler),
      decodeFailureHandler = decodeFailureHandler.getOrElse(DefaultDecodeFailureHandler.handler)
    )
    AwsServerInterpreter.toRoute(e)
  }

  override def routeRecoverErrors[I, E <: Throwable, O](e: Endpoint[I, E, O, Any], fn: I => IO[O])(implicit
      eClassTag: ClassTag[E]
  ): Route[IO] = {
    implicit val options: AwsServerOptions[IO] = AwsServerOptions.customInterceptors[IO]()
    AwsServerInterpreter.toRouteRecoverErrors(e)(fn)
  }

  override def server(routes: NonEmptyList[Route[IO]]): Resource[IO, Port] = Resource.eval(IO.pure(3000))
}
