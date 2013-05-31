package streamsend.pillar

import org.scalatest.{FunSpec, BeforeAndAfter}
import org.scalatest.matchers.ShouldMatchers
import java.io.FileInputStream
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

      it("assigns authoredAt") {
        val resource = new FileInputStream(migrationPath)
        MigrationParser().parse(resource).authoredAt should equal(new Date(1370028263))
      }

      it("assigns description") {
        val resource = new FileInputStream(migrationPath)
        MigrationParser().parse(resource).description should equal("creates views table")
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
  }
}
