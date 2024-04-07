package com.rockthejvm.reviewboard.services

import zio.*

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

import com.rockthejvm.reviewboard.domain.data.User
import com.rockthejvm.reviewboard.domain.data.UserToken
import com.rockthejvm.reviewboard.domain.errors.UnauthorizedException
import com.rockthejvm.reviewboard.repositories.RecoveryTokenRepository
import com.rockthejvm.reviewboard.repositories.UserRepository

trait UserService {
  def registerUser(email: String, password: String): Task[User]
  def verifyPassword(email: String, password: String): Task[Boolean]
  def updatePassword(email: String, oldPassword: String, newPassword: String): Task[User]
  def deleteUser(email: String, password: String): Task[User]
  def generateToken(email: String, password: String): Task[Option[UserToken]]
  def sendPasswordRecoveryEmain(email: String): Task[Unit]
  def recoverPasswordFromToken(email: String, token: String, newPassword: String): Task[Boolean]
}

class UserServiceLive private (
    jwtService: JWTService,
    emailService: EmailService,
    userRepository: UserRepository,
    tokenRepository: RecoveryTokenRepository
) extends UserService {

  override def registerUser(email: String, password: String): Task[User] =
    userRepository.create(
      User(id = -1L, email = email, hashedPassword = UserServiceLive.Hasher.generatedHash(password))
    )
  override def verifyPassword(email: String, password: String): Task[Boolean] =
    userRepository.getByEmail(email).map {
      case Some(user) => UserServiceLive.Hasher.validateHash(password, user.hashedPassword)
      case None       => false
    }

  override def updatePassword(email: String, oldPassword: String, newPassword: String): Task[User] =
    for
      user <- userRepository
        .getByEmail(email)
        .someOrFailException

      verified = UserServiceLive.Hasher.validateHash(oldPassword, user.hashedPassword)

      updatedUser <- userRepository
        .update(
          user.id,
          _.copy(hashedPassword = UserServiceLive.Hasher.generatedHash(newPassword))
        )
        .when(verified)
        .someOrFail(UnauthorizedException(s"Could not update password for user $email"))
    yield updatedUser

  override def deleteUser(email: String, password: String): Task[User] =
    for
      user <- userRepository
        .getByEmail(email)
        .someOrFailException

      verified = UserServiceLive.Hasher.validateHash(password, user.hashedPassword)

      updatedUser <- userRepository
        .delete(user.id)
        .when(verified)
        .someOrFail(UnauthorizedException(s"Could not delete password for user $email"))
    yield updatedUser

  override def generateToken(email: String, password: String): Task[Option[UserToken]] =
    for {
      user       <- userRepository.getByEmail(email).someOrFail(UnauthorizedException("Invalid credentials"))
      verified   <- ZIO.attempt(UserServiceLive.Hasher.validateHash(password, user.hashedPassword))
      maybetoken <- jwtService.createToken(user).when(verified)

    } yield maybetoken

  override def sendPasswordRecoveryEmain(email: String): Task[Unit] =
    tokenRepository.getToken(email).flatMap {
      case Some(token) =>
        emailService
          .sendPasswordRecoveryEmail(email, token)
          .unit
      case None =>
        ZIO.unit
    }

  override def recoverPasswordFromToken(
      email: String,
      token: String,
      newPassword: String
  ): Task[Boolean] =
    for
      existingUser <- userRepository.getByEmail(email).someOrFailException
      validToken   <- tokenRepository.checkToken(email, token)
      updatedUser <- userRepository
        .update(
          existingUser.id,
          _.copy(hashedPassword = UserServiceLive.Hasher.generatedHash(newPassword))
        )
        .when(validToken)
    yield updatedUser.nonEmpty

}

object UserServiceLive {
  val layer =
    ZLayer.fromFunction(UserServiceLive(_, _, _, _))

  object Hasher {

    private val PBKDF2_ALGORITHM  = "PBKDF2WithHmacSHA512"
    private val PBKDF2_ITERATIONS = 1000
    private val SALT_BYTE_SIZE    = 24
    private val HASH_BYTE_SIZE    = 24

    val skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)

    private def pbkdf2(
        password: Array[Char],
        salt: Array[Byte],
        iterations: Int,
        nBytes: Int
    ): Array[Byte] =

      val keySpec = PBEKeySpec(password, salt, iterations, nBytes * 8)
      skf.generateSecret(keySpec).getEncoded()

    private def toHex(array: Array[Byte]): String = array.map("%02X".format(_)).mkString

    private def fromHex(hex: String): Array[Byte] =
      hex.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)

    private def compareArrays(a1: Array[Byte], a2: Array[Byte]): Boolean =
      val range = 0 until math.min(a1.length, a2.length)
      val diff  = range.foldLeft(a1.length ^ a2.length)((acc, i) => acc | (a1(i) ^ a2(i)))
      diff == 0

    def generatedHash(password: String): String =
      val rng: SecureRandom = new SecureRandom()
      val salt: Array[Byte] = Array.ofDim[Byte](SALT_BYTE_SIZE)
      rng.nextBytes(salt) // fill the salt with SALT_BYTE_SIZE random bytes
      val hashBytes = pbkdf2(password.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE)
      s"$PBKDF2_ITERATIONS:${toHex(salt)}:${toHex(hashBytes)}"

    def validateHash(string: String, hash: String): Boolean =
      val hashSegments = hash.split(":")
      val iterations   = hashSegments(0).toInt
      val salt         = fromHex(hashSegments(1))
      val validHash    = fromHex(hashSegments(2))
      val testHash = pbkdf2(
        string.toCharArray(),
        salt,
        iterations,
        HASH_BYTE_SIZE
      )
      // toHex(testHash) == toHex(validHash)
      compareArrays(testHash, validHash)

  }
}
