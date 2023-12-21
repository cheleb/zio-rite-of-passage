package com.rockthejvm.reviewboard

import zio.*

import com.rockthejvm.reviewboard.http.controllers.HealthController
import zio.http.Server
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.server.ziohttp.ZioHttpServerOptions

object Application extends ZIOAppDefault:

  val serrverProgram =
    for
      _                <- ZIO.succeed(println("Hello world"))
      healthController <- HealthController.makeZIO
      _ <- Server.serve(
        ZioHttpInterpreter(ZioHttpServerOptions.default).toHttp(healthController.health)
      )
    yield ()

  override def run =
    serrverProgram
      .provide(
        Server.default
      )
