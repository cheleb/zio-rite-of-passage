package com.rockthejvm.reviewboard.repositories

import zio.*

import java.lang.System

import com.rockthejvm.reviewboard.config.Configs
import com.rockthejvm.reviewboard.config.RecoveryTokensConfig
import com.rockthejvm.reviewboard.domain.data.PasswordRecoveryToken

import io.getquill.*
import io.getquill.jdbczio.Quill

trait RecoveryTokenRepository {
  def getToken(email: String): Task[Option[String]]
  def checkToken(email: String, token: String): Task[Boolean]
}

class RecoveryTokenRepositoryLive private (
    config: RecoveryTokensConfig,
    quill: Quill.Postgres[SnakeCase],
    userRepository: UserRepository
) extends RecoveryTokenRepository {

  import quill.*

  inline given SchemaMeta[PasswordRecoveryToken] = schemaMeta("recovery_tokens")
  inline given InsertMeta[PasswordRecoveryToken] = insertMeta[PasswordRecoveryToken]()
  inline given UpdateMeta[PasswordRecoveryToken] = updateMeta[PasswordRecoveryToken](_.email)

  private val tokenDuration = config.duration

  private def randomUppercaseString(size: Int): Task[String] =
    ZIO.succeed(scala.util.Random.alphanumeric.take(size).mkString.toUpperCase)

  private inline def recoveryTokens = query[PasswordRecoveryToken]

  private def findToken(email: String): Task[Option[String]] = run(
    recoveryTokens
      .filter(_.email == lift(email))
      .map(_.token)
  ).map(_.headOption)

  private def replaceToken(email: String): Task[String] =
    for
      token <- randomUppercaseString(8)
      _ <- run(
        recoveryTokens
          .withFilter(_.email == lift(email))
          .updateValue(
            lift(PasswordRecoveryToken(email, token, System.currentTimeMillis() + tokenDuration))
          )
          .returning(r => r)
      )
    yield token
  private def generateToken(email: String): Task[String] =
    for
      token <- randomUppercaseString(8)
      _ <- run(
        recoveryTokens
          .insertValue(
            lift(PasswordRecoveryToken(email, token, System.currentTimeMillis() + tokenDuration))
          )
          .returning(r => r)
      )
    yield token
  // Find a token for a user, if so replace it with a fresh one, otherwise create a new one
  private def makeFreshToken(email: String): Task[String] =
    findToken(email).flatMap {
      case Some(value) =>
        replaceToken(email)
      case None => generateToken(email)
    }
  override def getToken(email: String): Task[Option[String]] =
    userRepository.getByEmail(email).flatMap {
      case None => ZIO.none
      case Some(value) =>
        makeFreshToken(email).map(Some(_))
    }
  override def checkToken(email: String, token: String): Task[Boolean] = for {
    now <- Clock.instant
    checkValid <- run(recoveryTokens.filter(r =>
      r.email == lift(email) && r.token == lift(token) && r.expiration > lift(now.toEpochMilli)
    ))
      .map(_.nonEmpty)

  } yield checkValid
}

object RecoveryTokenRepositoryLive:
  def layer = ZLayer.fromFunction(RecoveryTokenRepositoryLive(_, _, _))

  def configuredLayer =
    Configs.makeConfigLayer[RecoveryTokensConfig]("rockthejvm.recoverytokens") >>> layer
