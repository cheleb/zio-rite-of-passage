package com.rockthejvm.reviewboard.repositories

import zio.*
import zio.test.*
import zio.test.Assertion.*
import com.rockthejvm.reviewboard.domain.data.Company

import com.rockthejvm.reviewboard.syntax.*
import java.sql.SQLException
import com.rockthejvm.reviewboard.domain.data.CompanyFilter

object CompanyRepositorySpec extends ZIOSpecDefault with RepositorySpec("sql/companies.sql") {

  private val rockthejvm =
    Company(
      1,
      "rock-the-jvm",
      "Rock the JVM",
      "https://rockthejvm.com",
      country = Some("Romania"),
      tags = List("Scala")
    )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("CompanyRepositorySpec")(
      test("create a company") {
        val program = for {
          repository <- ZIO.service[CompanyRepository]
          company    <- repository.create(rockthejvm)

        } yield company

        program.assert(
          equalTo(rockthejvm)
        )
      },
      test("Create the same company twice should fail") {
        val program = for {
          repository <- ZIO.service[CompanyRepository]
          _          <- repository.create(rockthejvm)
          company    <- repository.create(rockthejvm).flip
        } yield company

        program.assert(
          _.isInstanceOf[SQLException]
        )
      },
      test("Search a company by location") {
        val program = for {
          repository <- ZIO.service[CompanyRepository]
          _          <- repository.create(rockthejvm)
          company <-
            repository.search(CompanyFilter(countries = List("Romania"), tags = List("Scala")))
        } yield company

        program.assert(
          _.nonEmpty
        )
      }
    ).provide(
      CompanyRepositoryLive.layer,
      Repository.quillLayer,
      dataSouurceLayer,
      Scope.default
    )

}
