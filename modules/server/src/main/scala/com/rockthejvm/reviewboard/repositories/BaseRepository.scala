package com.rockthejvm.reviewboard.repositories

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.Task

trait WithTransaction {
  def tx[A](zio: Task[A]): Task[A]
}

class BaseRepository(quill: Quill.Postgres[SnakeCase]) extends WithTransaction {
  def tx[A](zio: Task[A]): Task[A] = quill.transaction(zio)
}
