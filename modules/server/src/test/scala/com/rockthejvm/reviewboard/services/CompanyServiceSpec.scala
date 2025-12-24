package com.rockthejvm.reviewboard.services

import zio.*

import zio.test.*

import scala.collection.mutable

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.domain.data.CompanyFilter
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.repositories.CompanyRepository
import com.rockthejvm.reviewboard.syntax.*

object CompanyServiceSpec extends ZIOSpecDefault {

  private def service = ZIO.serviceWithZIO[CompanyService]

  private val repoStubLayer = ZLayer.succeed(
    new CompanyRepository {

      override def tx[A](zio: Task[A]): Task[A] = ???

      override def delete(id: Long): Task[Company] =
        ZIO.attempt {
          val oldCompany = db(id)
          db -= id
          oldCompany
        }

      override def update(id: Long, op: Company => Company): Task[Company] = ZIO.attempt {
        val oldCompany = db(id)
        val newCompany = op(oldCompany)
        db += (id -> newCompany)
        newCompany
      }

      private val db = mutable.Map.empty[Long, Company]

      override def create(company: Company): Task[Company] =
        val id            = db.keys.maxOption.getOrElse(0L) + 1
        val companyWithId = company.copy(id = id)
        db += (id -> companyWithId)
        ZIO.succeed(companyWithId)
      override def getById(id: Long): Task[Option[Company]] =
        ZIO.succeed(db.get(id))
      override def getBySlug(slug: String): Task[Option[Company]] =
        ZIO.succeed(db.values.find(_.slug == slug))
      override def getAll: Task[List[Company]] =
        ZIO.succeed(db.values.toList)
      override def uniqueAttributes: Task[CompanyFilter] = ???

      override def search(companyFilter: CompanyFilter): Task[List[Company]] = ???

    }
  )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("CompanyService")(
      test("create company") {
        val companyZIO =
          service(_.create(CreateCompanyRequest("Rock the JVM", "rockthejvm.com")))

        companyZIO.assert { company =>
          company.name == "Rock the JVM" &&
          company.slug == "rock-the-jvm" &&
          company.url == "rockthejvm.com"
        }
      },
      test("get company by id") {

        val program =
          for
            company    <- service(_.create(CreateCompanyRequest("Rock the JVM", "rockthejvm.com")))
            companyOpt <- service(_.getById(company.id))
          yield (company, companyOpt)

        program.assert {
          case (company, Some(companyRes)) =>
            company.name == "Rock the JVM" &&
            company.slug == "rock-the-jvm" &&
            company.url == "rockthejvm.com" &&
            companyRes == company
          case _ => false
        }

      },
      test("get company by slug") {

        val program =
          for
            company    <- service(_.create(CreateCompanyRequest("Rock the JVM", "rockthejvm.com")))
            companyOpt <- service(_.getBySlug(company.slug))
          yield (company, companyOpt)

        program.assert {
          case (company, Some(companyRes)) =>
            company.name == "Rock the JVM" &&
            company.slug == "rock-the-jvm" &&
            company.url == "rockthejvm.com" &&
            companyRes == company
          case _ => false
        }

      },
      test("get all companies") {

        val program =
          for
            company   <- service(_.create(CreateCompanyRequest("Rock the JVM", "rockthejvm.com")))
            company2  <- service(_.create(CreateCompanyRequest("Google", "google.com")))
            companies <- service(_.getAll)
          yield (company, company2, companies)

        program.assert { case (company, company2, companies) =>
          companies.toSet == Set(company, company2)
        }

      }
    ).provide(CompanyServiceLive.layer, repoStubLayer, ReviewServiceSpec.reviewRepositoryLayer)

}
