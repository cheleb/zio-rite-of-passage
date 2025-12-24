package com.rockthejvm.reviewboard.services

import zio.*

import zio.test.*

import com.rockthejvm.reviewboard.domain.data.User
import com.rockthejvm.reviewboard.domain.data.UserID
import com.rockthejvm.reviewboard.domain.data.UserToken
import com.rockthejvm.reviewboard.repositories.RecoveryTokenRepository
import com.rockthejvm.reviewboard.repositories.UserRepository
object UserServiceSpec extends ZIOSpecDefault {

  val daniel = User(
    1,
    "daniel@rockthejvm.com",
    "1000:398BC9021A891666236490F8CFA6C3DCBD835E0FF6B4E3BD:78DCCAABFAC75AE94538EA6DCD2D0AB8069FEEF7FACEAD96"
  )

  val stubEmailServiceLayer = ZLayer.succeed(
    new EmailService("http://localhost:1234") {
      override def sendEmail(email: String, subject: String, content: String): Task[Unit] = ZIO.unit
    }
  )

  val stubRecoveryTokenRepoLayer = ZLayer.succeed(
    new RecoveryTokenRepository {

      val db = collection.mutable.Map.empty[String, String]

      override def getToken(email: String): Task[Option[String]] = ZIO.attempt {
        val token = util.Random.alphanumeric.take(8).mkString
        db += (email -> token)
        Some(token)
      }

      override def checkToken(email: String, token: String): Task[Boolean] =
        ZIO.succeed(db.get(email).contains(token))

    }
  )

  val stubRepoLayer = ZLayer.succeed(
    new UserRepository {

      private val db = collection.mutable.Map(
        1L -> daniel
      )

      override def getById(id: Long): Task[Option[User]] = ZIO.succeed(db.get(id))

      override def create(user: User): Task[User] = ZIO.succeed {
        db += (user.id -> user)
        user
      }
      override def getByEmail(email: String): Task[Option[User]] = ZIO.succeed(
        db.values.find(_.email == email)
      )
      override def update(id: Long, update: User => User): Task[User] =
        for
          user <- getById(id).someOrFailException
          updatedUser = update(user)
          _ <- create(updatedUser)
        yield updatedUser
      override def delete(id: Long): Task[User] =
        for
          user <- getById(id).someOrFailException
          _    <- ZIO.succeed(db -= id)
        yield user
    }
  )

  val stubJWTLayer = ZLayer.succeed(
    new JWTService {
      override def createToken(user: User): Task[UserToken] = ZIO.succeed(
        UserToken(user.id, user.email, "BIG ACCESS", Long.MaxValue)
      )
      override def verifyToken(token: String): Task[UserID] =
        ZIO.succeed(UserID(daniel.id, daniel.email))
    }
  )
  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("UserService")(
      test("create and validate a user") {
        for
          userService <- ZIO.service[UserService]
          user        <- userService.registerUser(daniel.email, "rockthejvm")
          valid       <- userService.verifyPassword(daniel.email, "rockthejvm")
        yield assertTrue(valid && user.email == daniel.email)
      },
      test("validate correct credentials") {
        for
          userService <- ZIO.service[UserService]
          valid       <- userService.verifyPassword(daniel.email, "rockthejvm")
        yield assertTrue(valid)
      },
      test("invalidate incorrect credentials") {
        for
          userService <- ZIO.service[UserService]
          valid       <- userService.verifyPassword(daniel.email, "rockthejvm1")
        yield assertTrue(!valid)
      },
      test("invalidate incorrect email") {
        for
          userService <- ZIO.service[UserService]
          valid       <- userService.verifyPassword("nobody@nowhere.no", "rockthejvm")
        yield assertTrue(!valid)
      },
      test("update a password") {
        for
          userService <- ZIO.service[UserService]
          _           <- userService.updatePassword(daniel.email, "rockthejvm", "scala")
          oldValid    <- userService.verifyPassword(daniel.email, "rockthejvm")
          newValid    <- userService.verifyPassword(daniel.email, "scala")
        yield assertTrue(newValid && !oldValid)
      },
      test("delete non existing user should fail") {
        for
          userService <- ZIO.service[UserService]
          deletedUser <- userService.deleteUser("nobody@nowhere.no", "rockthejvm").flip
        yield assertTrue(deletedUser.isInstanceOf[RuntimeException])
      },
      test("delete user with incorect password should fail") {
        for
          userService <- ZIO.service[UserService]
          deletedUser <- userService.deleteUser(daniel.email, "hacked").flip
        yield assertTrue(deletedUser.isInstanceOf[RuntimeException])
      },
      test("delete a user") {
        for
          userService <- ZIO.service[UserService]
          deletedUser <- userService.deleteUser(daniel.email, "rockthejvm")
        yield assertTrue(deletedUser.email == daniel.email)
      }
    ).provide(
      UserServiceLive.layer,
      stubRepoLayer,
      stubJWTLayer,
      stubEmailServiceLayer,
      stubRecoveryTokenRepoLayer
    )

}
