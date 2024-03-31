package com.rockthejvm.reviewboard.core

import sttp.client3.impl.zio.FetchZioBackend
import sttp.client3.*
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.capabilities.zio.ZioStreams
import sttp.capabilities.WebSockets
import sttp.tapir.Endpoint
import sttp.model.Uri
import zio.*
import com.rockthejvm.reviewboard.config.*
import com.rockthejvm.reviewboard.http.endpoints.*

case class RestrictedEndpointException(message: String) extends RuntimeException(message)

/** A client to the backend, exposing the endpoints as methods.
  */
trait BackendClient {
  val company = new CompanyEndpoints {}
  val user    = new UserEndpoints {}
  val review  = new ReviewEndpoints {}
  val invite  = new InviteEndpoints {}

  def endpointRequestZIO[I, E <: Throwable, O](endpoint: Endpoint[Unit, I, E, O, Any])(
      payload: I
  ): Task[O]

  def securedEndpointRequestZIO[I, E <: Throwable, O](endpoint: Endpoint[String, I, E, O, Any])(payload: I): Task[O]

}
private class BackendClientLive(
    backend: SttpBackend[Task, ZioStreams & WebSockets],
    interpreter: SttpClientInterpreter,
    config: BackendClientConfig
) extends BackendClient {

  private def endpointRequest[I, E, O](endpoint: Endpoint[Unit, I, E, O, Any]): I => Request[Either[E, O], Any] =
    interpreter.toRequestThrowDecodeFailures(endpoint, config.baseUrl)

  private def securedEndpointRequest[A, I, E, O](endpoint: Endpoint[A, I, E, O, Any])
      : A => I => Request[Either[E, O], Any] =
    interpreter.toSecureRequestThrowDecodeFailures(endpoint, config.baseUrl)

  private def tokenOfFail =
    ZIO.fromOption(Session.getUserState)
      .orElseFail(RestrictedEndpointException("No token found"))
      .map(_.token)

  def endpointRequestZIO[I, E <: Throwable, O](endpoint: Endpoint[Unit, I, E, O, Any])(
      payload: I
  ): ZIO[Any, Throwable, O] =
    backend.send(endpointRequest(endpoint)(payload)).map(_.body).absolve

  def securedEndpointRequestZIO[I, E <: Throwable, O](endpoint: Endpoint[String, I, E, O, Any])(payload: I)
      : ZIO[Any, Throwable, O] = for {
    token <- tokenOfFail
    res   <- backend.send(securedEndpointRequest(endpoint)(token)(payload)).map(_.body).absolve
  } yield res

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
