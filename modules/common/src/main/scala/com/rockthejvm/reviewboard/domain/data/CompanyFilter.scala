package com.rockthejvm.reviewboard.domain.data

import zio.json.JsonCodec

final case class CompanyFilter(
    locations: List[String] = Nil,
    countries: List[String] = Nil,
    industries: List[String] = Nil,
    tags: List[String] = Nil
) derives JsonCodec {
  val isEmpty = locations.isEmpty && countries.isEmpty && industries.isEmpty && tags.isEmpty
  def groupNameChecked(groupName: String, value: String): Boolean = groupName match {
    case "Locations"  => locations.contains(value)
    case "Countries"  => countries.contains(value)
    case "Industries" => industries.contains(value)
    case "Tags"       => tags.contains(value)
    case _            => false
  }
}

object CompanyFilter {
  val empty = CompanyFilter()
}
