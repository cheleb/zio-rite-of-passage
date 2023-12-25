package com.rockthejvm.reviewboard.http.requests

import com.rockthejvm.reviewboard.domain.data.Company

final case class CreateCompanyRequest(
    name: String,
    url: String,
    location: Option[String] = None,
    country: Option[String] = None,
    industry: Option[String] = None,
    image: Option[String] = None,
    tags: List[String] = List.empty
):
  def toCompany(id: Long): Company =
    Company(id, Company.makeSlug(name), name, url, location, country, industry, image, tags)

object CreateCompanyRequest:
  import zio.json.JsonCodec
  import zio.json.DeriveJsonCodec

  given codec: JsonCodec[CreateCompanyRequest] = DeriveJsonCodec.gen
