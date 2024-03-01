package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import com.rockthejvm.reviewboard.domain.data.*

trait CompanyEndpoints extends BaseEndpoint:
  val createEndpoint = baseSecuredEndpoint.post
    .tag("Companies")
    .name("create")
    .in("companies")
    .description("Create a new company")
    .post
    .in(jsonBody[CreateCompanyRequest])
    .out(jsonBody[Company])

  val getAllEndpoint = baseEndpoint.get
    .tag("Companies")
    .name("getAll")
    .in("companies")
    .description("Get all companies")
    .get
    .out(jsonBody[List[Company]])

  val findByIdEndpoint = baseEndpoint.get
    .tag("Companies")
    .name("findById")
    .in("companies" / path[String]("id"))
    .description("Get a company by id or slug")
    .get
    .out(jsonBody[Option[Company]])

  val deleteEndpoint = baseSecuredEndpoint.delete
    .tag("Companies")
    .name("delete")
    .in("companies" / path[Long]("id"))
    .description("Delete a company by id")
    .delete
    .out(jsonBody[Company])

  val allFiltersEndpoint = baseEndpoint.get
    .tag("Companies")
    .name("allFilters")
    .in("companies" / "filters")
    .description("Get all available filters for companies")
    .get
    .out(jsonBody[CompanyFilter])

  val searchEndpoint =
    baseEndpoint.tag("Companies")
      .name("search")
      .description("Get companies based on filters")
      .in("companies" / "search")
      .post
      .in(jsonBody[CompanyFilter])
      .out(jsonBody[List[Company]])

  val companyEndpoints =
    List(
      createEndpoint,
      getAllEndpoint,
      searchEndpoint,
      findByIdEndpoint,
      deleteEndpoint,
      allFiltersEndpoint
    )
