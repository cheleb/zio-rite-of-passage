package com.rockthejvm.reviewboard

import zio.*

import com.rockthejvm.reviewboard.http.controllers.HealthController
import zio.http.Server
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.server.ziohttp.ZioHttpServerOptions

import com.rockthejvm.reviewboard.http.HttpApi
import com.rockthejvm.reviewboard.services.*
import com.rockthejvm.reviewboard.repositories.*
import com.rockthejvm.reviewboard.config.Configs
import com.rockthejvm.reviewboard.config.JWTConfig
import com.rockthejvm.reviewboard.config.EmailServiceConfig
import sttp.tapir.server.interceptor.cors.CORSInterceptor

object Application extends ZIOAppDefault:

  val serrverProgram =
    for
      _         <- ZIO.succeed(println("Hello world"))
      endpoints <- HttpApi.endpointsZIO
      _ <- Server.serve(
        ZioHttpInterpreter(ZioHttpServerOptions.default.appendInterceptor(
          CORSInterceptor.default
        )).toHttp(endpoints)
      )
    yield ()

  override def run =
    serrverProgram
      .provide(
        Server.default,
        // Service layers
        CompanyServiceLive.layer,
        ReviewServiceLive.layer,
        EmailServiceLive.configuredLayer,
        UserServiceLive.layer,
        InviteServiceLive.layer,
        JWTServiceLive.configuredLayer,
        RecoveryTokenRepositoryLive.configuredLayer,
        // Repository layers
        CompanyRepositoryLive.layer,
        ReviewRespositoryLive.layer,
        UserRepositoryLive.layer,
        InviteRepositoryLive.layer,
        Repository.dataLayer
      )
