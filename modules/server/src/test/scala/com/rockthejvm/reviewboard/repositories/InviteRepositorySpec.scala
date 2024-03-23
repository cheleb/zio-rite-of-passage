package com.rockthejvm.reviewboard.repositories

import zio.*
import zio.test.*
import zio.test.Assertion.*
import com.rockthejvm.reviewboard.syntax.*

object InviteRepositorySpec extends ZIOSpecDefault with RepositorySpec("sql/invites.sql") {

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("InviteRepositorySpec")(
      test("create an invite") {
        val program = for {
          repository <- ZIO.service[InviteRepository]
          invite     <- repository.addInvitePack("daniel", 1, 200)
        } yield invite

        program.assert(
          equalTo(1)
        )
      },
      test("create an invite pack") {
        val program = for {
          repository <- ZIO.service[InviteRepository]
          invite     <- repository.addInvitePack("daniel", 1, 200)
        } yield invite

        program.assert(
          equalTo(1)
        )
      },
      test("activate an invite pack") {
        val program = for {
          repository <- ZIO.service[InviteRepository]
          invite     <- repository.addInvitePack("daniel", 1, 200)
          activated  <- repository.activatePack(invite)
        } yield activated

        program.assert(
          equalTo(true)
        )
      }
    ).provide(
      InviteRepositoryLive.layer,
      dataSouurceLayer,
      Repository.quillLayer,
      Scope.default
    )

}
