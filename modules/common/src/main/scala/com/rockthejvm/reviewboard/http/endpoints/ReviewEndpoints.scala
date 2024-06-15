package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest

import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import com.rockthejvm.reviewboard.domain.data.ReviewSummary

object ReviewEndpoints extends BaseEndpoint {

  val createEndpoint =
    baseSecuredEndpoint.post
      .tag("reviews")
      .summary("Create a review")
      .in("reviews")
      .in(jsonBody[CreateReviewRequest])
      .out(jsonBody[Review])

  val getByIdEndpoint =
    baseEndpoint.get
      .tag("reviews")
      .summary("Get a review by id")
      .in("reviews" / path[Long]("id"))
      .out(jsonBody[Option[Review]])

  val getByCompanyIdEndpoint =
    baseEndpoint.get
      .tag("reviews")
      .summary("Get all reviews for a company")
      .in("reviews" / "company" / path[Long]("companyId"))
      .out(jsonBody[List[Review]])

  val getbyUserIdEndpoint =
    baseEndpoint.get
      .tag("reviews")
      .summary("Get all reviews for a user")
      .in("reviews" / "user" / path[Long]("userId"))
      .out(jsonBody[List[Review]])

  val getSummaryEndpoint =
    baseEndpoint.get
      .tag("reviews")
      .name("GetSummary")
      .description("Get a summary of reviews for a company")
      .in("reviews" / "company" / path[Long]("companyId") / "summary")
      .get
      .out(jsonBody[Option[ReviewSummary]])

  val makeSummaryEndpoint = baseSecuredEndpoint
    .tag("reviews")
    .name("MakeSummary")
    .description("Make a summary of reviews for a company")
    .in("reviews" / "company" / path[Long]("companyId") / "summary")
    .post
    .out(jsonBody[Option[ReviewSummary]])
}
