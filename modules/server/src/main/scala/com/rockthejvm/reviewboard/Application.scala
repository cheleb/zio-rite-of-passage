package com.rockthejvm.reviewboard

import com.rockthejvm.reviewboard.http.HttpApi
import com.rockthejvm.reviewboard.repositories.*
import com.rockthejvm.reviewboard.services.*
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.interceptor.log.DefaultServerLog
import sttp.tapir.server.interceptor.log.ServerLog
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.server.ziohttp.ZioHttpServerOptions
import zio.*
import zio.http.Server
import zio.logging.backend.SLF4J

object Application extends ZIOAppDefault:

  val serverLOg: ServerLog[Task] = DefaultServerLog(
    doLogWhenReceived = { s => ZIO.logInfo(s">>> $s") },
    doLogWhenHandled = { case (s, e) => ZIO.logInfo(s">>> $s: $e") },
    doLogAllDecodeFailures = { case (s, e) => ZIO.logInfo(s">>> $s: $e") },
    doLogExceptions = { case (s, e) => ZIO.logInfo(s">>> $s: $e") },
    noLog = ZIO.logInfo("........................."),
    logWhenReceived = true,
    logWhenHandled = true,
    logAllDecodeFailures = true
  )

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  val serrverProgram =
    for
      _         <- ZIO.logWarning("Running the server")
      endpoints <- HttpApi.endpointsZIO
      _ <- Server.serve(
        ZioHttpInterpreter(ZioHttpServerOptions
          .customiseInterceptors.serverLog(serverLOg).options
          .appendInterceptor(
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
        ReviewServiceLive.configuredLayer,
        EmailServiceLive.configuredLayer,
        UserServiceLive.layer,
        InviteServiceLive.configuredLayer,
        JWTServiceLive.configuredLayer,
        RecoveryTokenRepositoryLive.configuredLayer,
        PaymentServiceLive.configuredLayer,
        OpenAIServiceLive.configuredLayer,
        // Repository layers
        CompanyRepositoryLive.layer,
        ReviewRespositoryLive.layer,
        UserRepositoryLive.layer,
        InviteRepositoryLive.layer,
        Repository.dataLayer
      )
