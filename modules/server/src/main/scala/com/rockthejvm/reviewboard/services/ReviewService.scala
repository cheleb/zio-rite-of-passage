package com.rockthejvm.reviewboard.services

import zio.*

import com.rockthejvm.reviewboard.config.Configs
import com.rockthejvm.reviewboard.config.SummaryConfig
import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.repositories.ReviewRepository

trait ReviewService {
  def create(createReviewRequest: CreateReviewRequest, usedId: Long): Task[Review]
  def getById(reviewId: Long): Task[Option[Review]]
  def getByCompanyId(companyId: Long): Task[List[Review]]
  def getByUserId(userId: Long): Task[List[Review]]
  def getSummary(companyId: Long): Task[Option[ReviewSummary]]
  def makeSummary(companyId: Long, userId: Long): Task[Option[ReviewSummary]]
}

class ReviewServiceLive private (repo: ReviewRepository, openAIService: OpenAIService, config: SummaryConfig)
    extends ReviewService:
  override def create(createReviewRequest: CreateReviewRequest, usedId: Long): Task[Review] =
    repo.create(
      Review(
        id = -1L,
        companyId = createReviewRequest.companyId,
        userId = usedId,
        management = createReviewRequest.management,
        culture = createReviewRequest.culture,
        salary = createReviewRequest.salary,
        benefits = createReviewRequest.benefits,
        wouldRecommend = createReviewRequest.wouldRecommend,
        review = createReviewRequest.review,
        created = java.time.Instant.now(),
        updated = java.time.Instant.now()
      )
    )
  override def getById(reviewId: Long): Task[Option[Review]] =
    repo.getById(reviewId)
  override def getByCompanyId(companyId: Long): Task[List[Review]] =
    repo.getByCompanyId(companyId)
  override def getByUserId(userId: Long): Task[List[Review]] =
    repo.getByUserId(userId)

  override def getSummary(companyId: Long): Task[Option[ReviewSummary]] =
    repo.getSummary(companyId)
  override def makeSummary(companyId: Long, userId: Long): Task[Option[ReviewSummary]] =
    getByCompanyId(companyId).flatMap(reviews =>
      Random.shuffle(reviews)
    ).map(_.take(config.nSelected))
      .flatMap { reviews =>
        val currentSummary = if reviews.size < config.minReviews then
          ZIO.succeed(Some(s"Not enough reviews (${config.minReviews}) to make a summary"))
        else
          openAIService.getCompletion(buildPrompt(reviews))

        currentSummary.flatMap {
          case Some(summary) => repo.insertSummary(companyId, summary).option
          case None          => ZIO.succeed(None)
        }

      }

  private def buildPrompt(reviews: List[Review]): String =
    "You have the following reviews about a company:" +
      reviews.zipWithIndex.map {
        case (Review(_, _, _, management, culture, salary, benefits, wouldRecommend, review, _, _), index) =>
          s"""
             |Review ${index + 1}:
             |  Management: $management stars / 5
             |  Culture: $culture stars / 5
             |  Salary: $salary stars / 5
             |  Benefits: $benefits stars / 5
             |  Net promoter score: $wouldRecommend
             |  Content: "$review"
        """.stripMargin
      }.mkString("\n") +
      "Make a summary of the reviews above in at most one paragraph."

object ReviewServiceLive:
  val layer =
    ZLayer.fromFunction(ReviewServiceLive(_, _, _))

  val configuredLayer = Configs.makeConfigLayer[SummaryConfig]("rockthejvm.summary") >>> layer
