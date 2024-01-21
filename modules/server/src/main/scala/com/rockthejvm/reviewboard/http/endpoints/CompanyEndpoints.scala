package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import com.rockthejvm.reviewboard.domain.data.Company

trait CompanyEndpoints extends BaseEndpoint:
  val createEndpoint = baseEndpoint.post
    .tag("companies")
    .name("create")
    .in("companies")
    .description("Create a new company")
    .post
    .in(jsonBody[CreateCompanyRequest])
    .out(jsonBody[Company])

  val getAllEndpoint = baseEndpoint.get
    .tag("companies")
    .name("getAll")
    .in("companies")
    .description("Get all companies")
    .get
    .out(jsonBody[List[Company]])

  val findByIdEndpoint = baseEndpoint.get
    .tag("companies")
    .name("findById")
    .in("companies" / path[String]("id"))
    .description("Get a company by id or slug")
    .get
    .out(jsonBody[Option[Company]])

  val deleteEndpoint = baseEndpoint.delete
    .tag("companies")
    .name("delete")
    .in("companies" / path[Long]("id"))
    .description("Delete a company by id")
    .delete
    .out(jsonBody[Company])

  val companyEndpoints = List(createEndpoint, getAllEndpoint, findByIdEndpoint, deleteEndpoint)
