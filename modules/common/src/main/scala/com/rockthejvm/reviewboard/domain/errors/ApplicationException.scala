package com.rockthejvm.reviewboard.domain.errors

sealed abstract class ApplicationException(message: String) extends RuntimeException(message)

case object UnauthorizedException extends ApplicationException("Unauthorized")
