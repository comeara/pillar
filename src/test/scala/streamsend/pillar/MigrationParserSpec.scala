package streamsend.pillar

import org.scalatest.{FunSpec, BeforeAndAfter}
import org.scalatest.matchers.ShouldMatchers
import java.io.{ByteArrayInputStream, FileInputStream}
import java.util.Date

class MigrationParserSpec extends FunSpec with BeforeAndAfter with ShouldMatchers {
  describe("#parse") {
    describe("1370028262_creates_events_table.cql") {
      val migrationPath = "src/test/resources/pillar/migrations/faker/1370028262_creates_events_table.cql"

      it("returns a migration object") {
        val resource = new FileInputStream(migrationPath)
        MigrationParser().parse(resource).getClass should be(classOf[IrreversibleMigration])
      }

      it("assigns authoredAt") {
        val resource = new FileInputStream(migrationPath)
        MigrationParser().parse(resource).authoredAt should equal(new Date(1370023262))
      }

      it("assigns description") {
        val resource = new FileInputStream(migrationPath)
        MigrationParser().parse(resource).description should equal("creates events table")
      }

      it("assigns up") {
        val resource = new FileInputStream(migrationPath)
        MigrationParser().parse(resource).up should equal( """CREATE TABLE events (
                                                             |  batch_id text,
                                                             |  occurred_at uuid,
                                                             |  event_type text,
                                                             |  payload blob,
                                                             |  PRIMARY KEY (batch_id, occurred_at, event_type)
                                                             |)""".stripMargin)
      }
    }

    describe("1370028263_creates_views_table.cql") {
      val migrationPath = "src/test/resources/pillar/migrations/faker/1370028263_creates_views_table.cql"

      it("returns a migration object") {
        val resource = new FileInputStream(migrationPath)
        MigrationParser().parse(resource).getClass should be(classOf[ReversibleMigration])
      }

      it("assigns down") {
        val resource = new FileInputStream(migrationPath)
        MigrationParser().parse(resource).asInstanceOf[ReversibleMigration].down should equal("DROP TABLE views")
      }
    }

    describe("1370028264_adds_user_agent_to_views_table.cql") {
      val migrationPath = "src/test/resources/pillar/migrations/faker/1370028264_adds_user_agent_to_views_table.cql"

      it("returns a migration object") {
        val resource = new FileInputStream(migrationPath)
        MigrationParser().parse(resource).getClass should be(classOf[ReversibleMigrationWithNoOpDown])
      }
    }

    describe("a migration missing an up stanza") {
      val migrationContent = """-- description: creates events table
                               |-- authoredAt: 1370023262""".stripMargin

      it("raises an InvalidMigrationException") {
        val resource = new ByteArrayInputStream(migrationContent.getBytes)
        val thrown = intercept[InvalidMigrationException] { MigrationParser().parse(resource) }
        thrown.errors("up") should equal("must be present")
      }
    }

    describe("a migration missing a description stanza") {
      val migrationContent = "-- authoredAt: 1370023262"

      it("raises an InvalidMigrationException") {
        val resource = new ByteArrayInputStream(migrationContent.getBytes)
        val thrown = intercept[InvalidMigrationException] { MigrationParser().parse(resource) }
        thrown.errors("description") should equal("must be present")
      }
    }

    describe("a migration missing an authoredAt stanza") {
      val migrationContent = "-- description: creates events table"

      it("raises an InvalidMigrationException") {
        val resource = new ByteArrayInputStream(migrationContent.getBytes)
        val thrown = intercept[InvalidMigrationException] { MigrationParser().parse(resource) }
        thrown.errors("authoredAt") should equal("must be present")
      }
    }

    describe("a migration with a bogus authored at stanza") {
      val migrationContent = "-- authoredAt: a long, long time ago"

      it("raises an InvalidMigrationException") {
        val resource = new ByteArrayInputStream(migrationContent.getBytes)
        val thrown = intercept[InvalidMigrationException] { MigrationParser().parse(resource) }
        thrown.errors("authoredAt") should equal("must be a number greater than zero")
      }
    }
  }
}