package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import com.rockthejvm.reviewboard.domain.data.Company

trait CompanyEndpoints:
  val createEndpoint = endpoint.post
    .tag("companies")
    .name("create")
    .in("companies")
    .description("Create a new company")
    .post
    .in(jsonBody[CreateCompanyRequest])
    .out(jsonBody[Company])

  val getAllEndpoint = endpoint.get
    .tag("companies")
    .name("getAll")
    .in("companies")
    .description("Get all companies")
    .get
    .out(jsonBody[List[Company]])

  val findByIdEndpoint = endpoint.get
    .tag("companies")
    .name("findById")
    .in("companies" / path[String]("id"))
    .description("Get a company by id or slug")
    .get
    .out(jsonBody[Option[Company]])

  val companyEndpoints = List(createEndpoint, getAllEndpoint, findByIdEndpoint)
