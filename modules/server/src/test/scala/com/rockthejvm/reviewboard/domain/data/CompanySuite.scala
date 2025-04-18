package com.rockthejvm.reviewboard.domain.data

import zio.test.*

// For more information on writing tests, see
// https://scalameta.org/munit/docs/getting-started.html

object HelloWorldSpec extends ZIOSpecDefault {
  def spec =
    suite("HelloWorldSpec")(
      test("make slug works") {
        assertTrue(Company.makeSlug("My ZIO  Inc") == "my-zio-inc")

      }
    )
}
