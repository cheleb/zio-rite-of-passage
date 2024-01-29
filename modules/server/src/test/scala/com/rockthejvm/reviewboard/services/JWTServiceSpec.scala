package com.rockthejvm.reviewboard.services

import zio.*
import zio.test.*

import com.rockthejvm.reviewboard.config.JWTConfig
import com.rockthejvm.reviewboard.domain.data.User

object JWTServiceSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("JWTService")(
      test("generate a token") {
        for
          jwtService <- ZIO.service[JWTService]
          token      <- jwtService.createToken(User(1L, "daniel@rockthejvm.com", "unimporteant"))
          userID     <- jwtService.verifyToken(token.token)
        yield assertTrue(
          userID.id == 1L && userID.email == "daniel@rockthejvm.com"
        )
      }
    ).provide(
      JWTServiceLive.layer,
      ZLayer.succeed(JWTConfig("secret", "rtj", 1.hour))
    )

}
