package com.rockthejvm.reviewboard.config

import zio.Config
import zio.config.magnolia.DeriveConfig
import zio.config.magnolia.deriveConfig

import sttp.model.Uri

final case class OpenIAConfig(baseUrl: Option[Uri], apiKey: String)

object OpenIAConfig {
  given DeriveConfig[Uri] =
    DeriveConfig.given_DeriveConfig_String.mapOrFail(Uri.parse(_).left.map(_ =>
      Config.Error.InvalidData(message = "Invalid URI")
    ))
  given Config[OpenIAConfig] = deriveConfig[OpenIAConfig]
}
