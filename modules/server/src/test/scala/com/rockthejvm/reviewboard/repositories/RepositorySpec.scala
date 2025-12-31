package com.rockthejvm.reviewboard.repositories

import zio.*

import javax.sql.DataSource

import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/** A trait that provides a PostgreSQL container for integration tests.
  */
trait RepositorySpec(init: String) {
  private def postgres() =
    val container = new PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"))
    container.withInitScript(init)
    container.start()
    container

  private def createDataSource(container: PostgreSQLContainer): DataSource =
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
