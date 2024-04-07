package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.repositories.CompanyRepository
import com.rockthejvm.reviewboard.repositories.ReviewRepository
import zio.*

trait CompanyService {
  def create(req: CreateCompanyRequest): Task[Company]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]
  def getAll: Task[List[Company]]
  def delete(id: Long): Task[Company]
  def allFilters: Task[CompanyFilter]
  def search(companyFilter: CompanyFilter): Task[List[Company]]
}

class CompanyServiceLive private (
    companyRepository: CompanyRepository,
    reviewRepository: ReviewRepository
) extends CompanyService {
  override def create(req: CreateCompanyRequest): Task[Company] =
    companyRepository.create(req.toCompany(-1L))
  override def getById(id: Long): Task[Option[Company]] =
    companyRepository.getById(id)
  override def getBySlug(slug: String): Task[Option[Company]] =
    companyRepository.getBySlug(slug)
  override def getAll: Task[List[Company]] =
    companyRepository.getAll

  override def search(companyFilter: CompanyFilter): Task[List[Company]] =
    companyRepository.search(companyFilter)

  override def delete(id: Long): Task[Company] =
    companyRepository.tx(
      for {
        company <- companyRepository
          .delete(id)
        _ <- reviewRepository.deleteByCompanyId(id)
      } yield company
    )
  override def allFilters: Task[CompanyFilter] = companyRepository.uniqueAttributes
}

object CompanyServiceLive {

  val layer: URLayer[CompanyRepository & ReviewRepository, CompanyServiceLive] =
    ZLayer.fromFunction(CompanyServiceLive(_, _))
}
