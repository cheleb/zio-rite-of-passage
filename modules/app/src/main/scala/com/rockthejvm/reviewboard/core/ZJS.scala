package com.rockthejvm.reviewboard.core

import sttp.client3.impl.zio.FetchZioBackend
import sttp.client3.*
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.capabilities.zio.ZioStreams
import sttp.capabilities.WebSockets
import sttp.tapir.Endpoint
import sttp.model.Uri
import zio.*
import com.rockthejvm.reviewboard.config.BackendClientConfig
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object ZJS {

  def useBackend[A] =
    ZIO.serviceWithZIO[BackendClient]

  extension [E <: Throwable, A](zio: ZIO[BackendClient, E, A])
    def emitTo(bus: EventBus[A]) =
      Unsafe.unsafe { implicit unsafe =>
        Runtime.default.unsafe.fork(zio.tapError(e => Console.printLineError(e.getMessage())).tap(
          a => ZIO.attempt(bus.emit(a))
        ).provide(BackendClientLive.configuredLayer))
      }
    def runJs =
      Unsafe.unsafe { implicit unsafe =>
        Runtime.default.unsafe.runToFuture(zio.provide(BackendClientLive.configuredLayer))
      }
  extension [I, E <: Throwable, O](endpoint: Endpoint[Unit, I, E, O, Any])
    def apply(payload: I): RIO[BackendClient, O] =
      ZIO.service[BackendClient].flatMap(_.endpointRequestZIO(endpoint)(payload))

}
