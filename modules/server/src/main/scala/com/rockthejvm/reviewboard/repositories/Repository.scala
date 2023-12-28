package com.rockthejvm.reviewboard.repositories

import io.getquill.jdbczio.Quill
import io.getquill.SnakeCase
import zio.ZLayer
import io.getquill.jdbczio.Quill.Postgres

object Repository {

  private def quillLayer = Quill.Postgres.fromNamingStrategy(SnakeCase)

  private def datasourceLayer = Quill.DataSource.fromPrefix("rockthejvm.db")

  def dataLayer: ZLayer[Any, Throwable, Postgres[SnakeCase.type]] = datasourceLayer >>> quillLayer
}
