package com.rockthejvm.reviewboard.services

import zio.*

import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.repositories.UserRepository
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

trait UserService {
  def registerUser(email: String, password: String): Task[User]
  def verifyPassword(email: String, password: String): Task[Boolean]
  def generateToken(email: String, password: String): Task[Option[UserToken]]
}

class UserServiceLive private (jwtService: JWTService, userRepository: UserRepository)
    extends UserService {

  override def registerUser(email: String, password: String): Task[User] =
    userRepository.create(
      User(id = -1L, email = email, hashedPassword = UserServiceLive.Hasher.generatedHash(password))
    )
  override def verifyPassword(email: String, password: String): Task[Boolean] =
    userRepository.getByEmail(email).map {
      case Some(user) => user.hashedPassword == UserServiceLive.Hasher.generatedHash(password)
      case None       => false
    }
  override def generateToken(email: String, password: String): Task[Option[UserToken]] =
    for {
      user       <- userRepository.getByEmail(email).someOrFailException
      verified   <- ZIO.attempt(UserServiceLive.Hasher.validateHash(password, user.hashedPassword))
      maybetoken <- jwtService.createToken(user).when(verified)

    } yield maybetoken

}

object UserServiceLive {
  val layer: URLayer[JWTService & UserRepository, UserServiceLive] =
    ZLayer.fromFunction(UserServiceLive(_, _))

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

object Demo extends App {
  println(UserServiceLive.Hasher.generatedHash("rockthejvm"))
  println(
    UserServiceLive.Hasher
      .validateHash(
        "rockthejvm",
        "1000:BA0A296552E26BB5DA77F084940B70AFF3F4429F99AD8E52:486AD9326B322A0D5D4ECBDCD751D6A51C1D9425C804A626"
      )
  )
}
