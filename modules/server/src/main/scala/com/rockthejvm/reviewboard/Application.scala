package com.rockthejvm.reviewboard

import zio.*

import com.rockthejvm.reviewboard.http.controllers.HealthController
import zio.http.Server
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.server.ziohttp.ZioHttpServerOptions

import com.rockthejvm.reviewboard.http.HttpApi
import com.rockthejvm.reviewboard.services.CompanyService
import com.rockthejvm.reviewboard.services.CompanyServiceLive
import com.rockthejvm.reviewboard.repositories.Repository
import com.rockthejvm.reviewboard.repositories.CompanyRepositoryLive
import com.rockthejvm.reviewboard.services.ReviewServiceLive
import com.rockthejvm.reviewboard.repositories.ReviewRespositoryLive

object Application extends ZIOAppDefault:

  val serrverProgram =
    for
      _         <- ZIO.succeed(println("Hello world"))
      endpoints <- HttpApi.endpointsZIO
      _ <- Server.serve(
        ZioHttpInterpreter(ZioHttpServerOptions.default).toHttp(endpoints)
      )
    yield ()

  override def run =
    serrverProgram
      .provide(
        Server.default,
        // Service layers
        CompanyServiceLive.layer,
        ReviewServiceLive.layer,
        // Repository layers
        CompanyRepositoryLive.layer,
        ReviewRespositoryLive.layer,
        Repository.dataLayer
      )
