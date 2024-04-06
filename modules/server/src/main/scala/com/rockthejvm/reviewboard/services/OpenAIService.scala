package com.rockthejvm.reviewboard.services

import zio.*

import sttp.capabilities.zio.ZioStreams
import sttp.client3.*
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import sttp.tapir.client.sttp.SttpClientInterpreter

import com.rockthejvm.reviewboard.config.OpenIAConfig
import com.rockthejvm.reviewboard.config.Configs
import com.rockthejvm.reviewboard.domain.errors.HttpError
import com.rockthejvm.reviewboard.http.requests.*

trait OpenAIService {
  def getCompletion(prompt: String): Task[Option[String]]
}

class OpenAIServiceLive private (
    backend: SttpBackend[Task, ZioStreams],
    interpreter: SttpClientInterpreter,
    config: OpenIAConfig
) extends OpenAIService {

  private val completionEndpoint = endpoint
    .errorOut(statusCode and plainBody[String])
    .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)
    .securityIn(auth.bearer[String]())
    .in("v1" / "chat" / "completions")
    .post
    .in(jsonBody[CompletionRequest])
    .out(jsonBody[CompletionResponse])

  private def secureEndpointRequest[S, I, E, O](endpoint: Endpoint[S, I, E, O, Any])
      : S => I => Request[Either[E, O], Any] =
    interpreter.toSecureRequestThrowDecodeFailures(endpoint, config.baseUrl)

  private def secureEndpointRequestZIO[I, E <: Throwable, O](endpoint: Endpoint[String, I, E, O, Any])(payload: I)
      : Task[O] =
    backend.send(secureEndpointRequest(endpoint)(config.apiKey)(payload)).map(_.body).absolve

  override def getCompletion(prompt: String): Task[Option[String]] =
    secureEndpointRequestZIO(completionEndpoint)(
      CompletionRequest(
        List(CompletionMessage(prompt)),
        "gpt-4"
      )
    ).map(_.choices.headOption.map(_.message.content))
}

object OpenAIServiceLive {
  val layer =
    ZLayer.fromFunction(OpenAIServiceLive(_, _, _))

  val configuredLayer = {
    HttpClientZioBackend.layer() ++
      ZLayer.succeed(SttpClientInterpreter()) ++
      Configs.makeConfigLayer[OpenIAConfig]("rockthejvm.openai") >>> layer

  }
}
