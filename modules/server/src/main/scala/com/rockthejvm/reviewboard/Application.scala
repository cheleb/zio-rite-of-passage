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
import com.rockthejvm.reviewboard.services.UserServiceLive
import com.rockthejvm.reviewboard.services.JWTServiceLive
import com.rockthejvm.reviewboard.repositories.UserRepositoryLive
import com.rockthejvm.reviewboard.config.Configs
import com.rockthejvm.reviewboard.config.JWTConfig
import com.rockthejvm.reviewboard.services.EmailServiceLive
import com.rockthejvm.reviewboard.repositories.RecoveryTokenRepositoryLive
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
        JWTServiceLive.configuredLayer,
        RecoveryTokenRepositoryLive.configuredLayer,
        // Repository layers
        CompanyRepositoryLive.layer,
        ReviewRespositoryLive.layer,
        UserRepositoryLive.layer,
        Repository.dataLayer
      )
