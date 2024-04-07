package com.rockthejvm.reviewboard.repositories

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import io.getquill.jdbczio.Quill.Postgres
import zio.ZLayer

object Repository {

  def quillLayer = Quill.Postgres.fromNamingStrategy(SnakeCase)

  private def datasourceLayer = Quill.DataSource.fromPrefix("rockthejvm.db")

  def dataLayer: ZLayer[Any, Throwable, Postgres[SnakeCase.type]] = datasourceLayer >>> quillLayer
}
