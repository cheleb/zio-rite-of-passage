package com.rockthejvm.reviewboard.repositories

import org.testcontainers.containers.PostgreSQLContainer
import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource
import java.sql.SQLException

import zio.*
import zio.test.*
import zio.test.Assertion.*

/** A trait that provides a PostgreSQL container for integration tests.
  */
trait RepositorySpec(init: String) {
  private def postgres(): PostgreSQLContainer[Nothing] =
    val container: PostgreSQLContainer[Nothing] = PostgreSQLContainer("postgres")
      .withInitScript(init)
    container.start()
    container

  private def createDataSource(container: PostgreSQLContainer[Nothing]): DataSource =
    val dataSource = new PGSimpleDataSource()
    dataSource.setUrl(container.getJdbcUrl)
    dataSource.setUser(container.getUsername)
    dataSource.setPassword(container.getPassword)
    dataSource

  val dataSouurceLayer = ZLayer {
    for {
      container <- ZIO.acquireRelease(ZIO.attempt(postgres()))(container =>
        ZIO.attempt(container.stop()).ignoreLogged
      )
    } yield createDataSource(container)
  }
}
