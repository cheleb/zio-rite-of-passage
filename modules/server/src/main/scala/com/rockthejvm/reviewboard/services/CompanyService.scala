package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest

import com.rockthejvm.reviewboard.domain.data.Company

import scala.collection.mutable

import zio.*
import com.rockthejvm.reviewboard.repositories.CompanyRepository

trait CompanyService {
  def create(req: CreateCompanyRequest): Task[Company]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]
  def getAll: Task[List[Company]]
}

class CompanyServiceLive private (companyRepository: CompanyRepository) extends CompanyService {
  override def create(req: CreateCompanyRequest): Task[Company] =
    companyRepository.create(req.toCompany(-1L))
  override def getById(id: Long): Task[Option[Company]] =
    companyRepository.getById(id)
  override def getBySlug(slug: String): Task[Option[Company]] =
    companyRepository.getBySlug(slug)
  override def getAll: Task[List[Company]] =
    companyRepository.getAll
}

object CompanyServiceLive {

  val layer: URLayer[CompanyRepository, CompanyServiceLive] =
    ZLayer.fromFunction(CompanyServiceLive(_))
}
