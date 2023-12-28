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

object CompanyService {
  val dummy = ZLayer.succeed(new CompanyServiceDummy)
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

class CompanyServiceDummy extends CompanyService {
  private val db = mutable.Map.empty[Long, Company]

  override def create(req: CreateCompanyRequest): Task[Company] =
    val id            = db.keys.maxOption.getOrElse(0L) + 1
    val companyWithId = req.toCompany(id)
    db += (id -> companyWithId)
    ZIO.succeed(companyWithId)
  override def getById(id: Long): Task[Option[Company]] =
    ZIO.succeed(db.get(id))
  override def getBySlug(slug: String): Task[Option[Company]] =
    ZIO.succeed(db.values.find(_.slug == slug))
  override def getAll: Task[List[Company]] =
    ZIO.succeed(db.values.toList)
}
