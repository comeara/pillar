package com.chrisomeara.pillar

import org.scalatest.{FunSpec, BeforeAndAfter}
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import org.scalatest.mock.MockitoSugar
import java.util.Date

class RegistrySpec extends FunSpec with BeforeAndAfter with ShouldMatchers with MockitoSugar {
  describe(".fromDirectory") {
    describe("without a reporter parameter") {
      describe("with a directory that exists and has migration files") {
        it("returns a registry with migrations") {
          val registry = Registry.fromDirectory(new File("src/test/resources/pillar/migrations/faker/"))
          registry.all.size should equal(4)
        }
      }

      describe("with a directory that does not exist") {
        it("returns an empty registry") {
          val registry = Registry.fromDirectory(new File("bogus"))
          registry.all.size should equal(0)
        }
      }
    }

    describe("with a reporter parameter") {
      val reporter = mock[Reporter]
      it("returns a registry populated with reporting migrations") {
        val registry = Registry.fromDirectory(new File("src/test/resources/pillar/migrations/faker/"), reporter)
        registry.all(0).getClass should be(classOf[ReportingMigration])
      }
    }
  }

  describe("#all") {
    val now = new Date()
    val oneSecondAgo = new Date(now.getTime - 1000)
    val migrations = List(
      Migration("test now", now, Seq("up")),
      Migration("test just before", oneSecondAgo, Seq("up"))
    )
    val registry = new Registry(migrations)

    it("sorts migrations by their authoredAt property ascending") {
      registry.all.map(_.authoredAt) should equal(List(oneSecondAgo, now))
    }
  }
}
