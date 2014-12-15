package com.chrisomeara.pillar

import org.scalatest.{FunSpec, BeforeAndAfter}
import org.scalatest.matchers.ShouldMatchers
import java.io.{ByteArrayInputStream, FileInputStream}
import java.util.Date

class ParserSpec extends FunSpec with BeforeAndAfter with ShouldMatchers {
  describe("#parse") {
    describe("1370028262_creates_events_table.cql") {
      val migrationPath = "src/test/resources/pillar/migrations/faker/1370028262_creates_events_table.cql"

      it("returns a migration object") {
        val resource = new FileInputStream(migrationPath)
        Parser().parse(resource).getClass should be(classOf[IrreversibleMigration])
      }

      it("assigns authoredAt") {
        val resource = new FileInputStream(migrationPath)
        Parser().parse(resource).authoredAt should equal(new Date(1370023262))
      }

      it("assigns description") {
        val resource = new FileInputStream(migrationPath)
        Parser().parse(resource).description should equal("creates events table")
      }

      it("assigns up") {
        val resource = new FileInputStream(migrationPath)
        Parser().parse(resource).up should contain( """CREATE TABLE events (
                                                             |  batch_id text,
                                                             |  occurred_at uuid,
                                                             |  event_type text,
                                                             |  payload blob,
                                                             |  PRIMARY KEY (batch_id, occurred_at, event_type)
                                                             |)""".stripMargin)
      }
    }

    describe("1370028265_creates_events_table_with_stages.cql") {
      val migrationPath = "src/test/resources/pillar/migrations/faker/1370028265_creates_events_table_with_stages.cql"

      it("returns a migration object") {
        val resource = new FileInputStream(migrationPath)
        Parser().parse(resource).getClass should be(classOf[ReversibleMigration])
      }

      it("assigns authoredAt") {
        val resource = new FileInputStream(migrationPath)
        Parser().parse(resource).authoredAt should equal(new Date(1370023265))
      }

      it("assigns description") {
        val resource = new FileInputStream(migrationPath)
        Parser().parse(resource).description should equal("creates events table with stages")
      }

      it("assigns two up stages") {
        val resource = new FileInputStream(migrationPath)
        val migration = Parser().parse(resource)

        migration.up should contain( """CREATE TABLE events (
                                        |  batch_id text,
                                        |  occurred_at uuid,
                                        |  event_type text,
                                        |  payload blob,
                                        |  PRIMARY KEY (batch_id, occurred_at, event_type)
                                        |)""".stripMargin)

        migration.up should contain( """CREATE TABLE events (
                                        |  batch_id text,
                                        |  occurred_at uuid,
                                        |  event_type text,
                                        |  payload blob,
                                        |  PRIMARY KEY (batch_id, occurred_at, event_type)
                                        |)""".stripMargin)
      }

      it("assigns two down stages") {
        val resource = new FileInputStream(migrationPath)
        val migration = Parser().parse(resource).asInstanceOf[ReversibleMigration]

        migration.down should contain( """DROP TABLE events""".stripMargin)
        migration.down should contain( """DROP TABLE events2""".stripMargin)
      }
    }

    describe("1370028263_creates_views_table.cql") {
      val migrationPath = "src/test/resources/pillar/migrations/faker/1370028263_creates_views_table.cql"

      it("returns a migration object") {
        val resource = new FileInputStream(migrationPath)
        Parser().parse(resource).getClass should be(classOf[ReversibleMigration])
      }

      it("assigns down") {
        val resource = new FileInputStream(migrationPath)
        Parser().parse(resource).asInstanceOf[ReversibleMigration].down should contain("DROP TABLE views")
      }
    }

    describe("1370028264_adds_user_agent_to_views_table.cql") {
      val migrationPath = "src/test/resources/pillar/migrations/faker/1370028264_adds_user_agent_to_views_table.cql"

      it("returns a migration object") {
        val resource = new FileInputStream(migrationPath)
        Parser().parse(resource).getClass should be(classOf[ReversibleMigrationWithNoOpDown])
      }
    }

    describe("a migration missing an up stanza") {
      val migrationContent = """-- description: creates events table
                               |-- authoredAt: 1370023262""".stripMargin

      it("raises an InvalidMigrationException") {
        val resource = new ByteArrayInputStream(migrationContent.getBytes)
        val thrown = intercept[InvalidMigrationException] { Parser().parse(resource) }
        thrown.errors("up") should equal("must be present")
      }
    }

    describe("a migration missing a description stanza") {
      val migrationContent = "-- authoredAt: 1370023262"

      it("raises an InvalidMigrationException") {
        val resource = new ByteArrayInputStream(migrationContent.getBytes)
        val thrown = intercept[InvalidMigrationException] { Parser().parse(resource) }
        thrown.errors("description") should equal("must be present")
      }
    }

    describe("a migration missing an authoredAt stanza") {
      val migrationContent = "-- description: creates events table"

      it("raises an InvalidMigrationException") {
        val resource = new ByteArrayInputStream(migrationContent.getBytes)
        val thrown = intercept[InvalidMigrationException] { Parser().parse(resource) }
        thrown.errors("authoredAt") should equal("must be present")
      }
    }

    describe("a migration with a bogus authored at stanza") {
      val migrationContent = "-- authoredAt: a long, long time ago"

      it("raises an InvalidMigrationException") {
        val resource = new ByteArrayInputStream(migrationContent.getBytes)
        val thrown = intercept[InvalidMigrationException] { Parser().parse(resource) }
        thrown.errors("authoredAt") should equal("must be a number greater than zero")
      }
    }
  }
}