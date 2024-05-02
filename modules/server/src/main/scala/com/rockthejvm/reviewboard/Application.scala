package com.rockthejvm.reviewboard

import zio.*
import zio.http.Server

import zio.logging.backend.SLF4J

import com.rockthejvm.reviewboard.http.HttpApi
import com.rockthejvm.reviewboard.repositories.*
import com.rockthejvm.reviewboard.services.*
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.interceptor.log.DefaultServerLog
import sttp.tapir.server.interceptor.log.ServerLog
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.server.ziohttp.ZioHttpServerOptions
import com.rockthejvm.reviewboard.config.Configs
import com.rockthejvm.reviewboard.config.HttpConfig
import java.net.InetSocketAddress

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

  private val configuredServer = Configs.makeConfigLayer[HttpConfig]("rockthejvm.http")
    >>> ZLayer(ZIO.service[HttpConfig]
      .map(config => Server.Config.default.copy(address = InetSocketAddress(config.port))))
    >>> Server.live
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  val runMigrations = for {
    flyway <- ZIO.service[FlywayService]
    _ <- flyway.runMigrations()
      .catchSome {
        case e => ZIO.logError(s"Error running migrations: ${e.getMessage()}")
            *> flyway.runRepair() *> flyway.runMigrations()
      }
  } yield ()

  val startServer =
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

  val program = for {
    _ <- runMigrations
    _ <- startServer
  } yield ()

  override def run =
    program
      .provide(
        configuredServer,
        // Service layers
        FlywayServiceLive.configuredLayer,
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
