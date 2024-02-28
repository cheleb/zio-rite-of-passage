package com.rockthejvm.reviewboard.core

import sttp.client3.impl.zio.FetchZioBackend
import sttp.client3.*
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.capabilities.zio.ZioStreams
import sttp.capabilities.WebSockets
import sttp.tapir.Endpoint
import sttp.model.Uri
import zio.*
import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import com.rockthejvm.reviewboard.config.*

trait BackendClient {
  val company = new CompanyEndpoints {}
  def endpointRequestZIO[I, E <: Throwable, O](endpoint: Endpoint[Unit, I, E, O, Any])(
      payload: I
  ): ZIO[Any, Throwable, O]
}
class BackendClientLive(
    backend: SttpBackend[Task, ZioStreams & WebSockets],
    interpreter: SttpClientInterpreter,
    config: BackendClientConfig
) extends BackendClient {

  private def endpointRequest[I, E, O](endpoint: Endpoint[Unit, I, E, O, Any])
      : I => Request[Either[E, O], Any] =
    interpreter.toRequestThrowDecodeFailures(endpoint, config.baseUrl)

  def endpointRequestZIO[I, E <: Throwable, O](endpoint: Endpoint[Unit, I, E, O, Any])(
      payload: I
  ): ZIO[Any, Throwable, O] =
    backend.send(endpointRequest(endpoint)(payload)).map(_.body).absolve

}

object BackendClientLive {
  val layer = ZLayer.fromFunction(BackendClientLive(_, _, _))

  val configuredLayer = {
    val backend     = FetchZioBackend()
    val interpreter = SttpClientInterpreter()
    val config      = BackendClientConfig(Some(uri"http://localhost:8080"))

    ZLayer.succeed(backend) ++ ZLayer.succeed(interpreter) ++ ZLayer.succeed(config) >>> layer
  }

}
