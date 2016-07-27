package com.chrisomeara.pillar

import com.datastax.driver.core.Session
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.io.{ByteArrayOutputStream, PrintStream}
import java.util.Date

import org.scalatest.mock.MockitoSugar

class PrintStreamReporterSpec extends FunSpec with MockitoSugar with Matchers with OneInstancePerTest {
  val session = mock[Session]
  val migration = Migration("creates things table", new Date(1370489972546L), Seq("up"), Some(Seq("down")))
  val output = new ByteArrayOutputStream()
  val stream = new PrintStream(output)
  val reporter = new PrintStreamReporter(stream)
  val keyspace = "myks"

  describe("#initializing") {
    it("should print to the stream") {
      reporter.initializing(session, keyspace, ReplicationOptions.default)
      output.toString should equal("Initializing myks\n")
    }
  }

  describe("#migrating") {
    describe("without date restriction") {
      it("should print to the stream") {
        reporter.migrating(session, None)
        output.toString should equal("Migrating with date restriction None\n")
      }
    }
  }

  describe("#applying") {
    it("should print to the stream") {
      reporter.applying(migration)
      output.toString should equal("Applying 1370489972546: creates things table\n")
    }
  }

  describe("#reversing") {
    it("should print to the stream") {
      reporter.reversing(migration)
      output.toString should equal("Reversing 1370489972546: creates things table\n")
    }
  }

  describe("#destroying") {
    it("should print to the stream") {
      reporter.destroying(session, keyspace)
      output.toString should equal("Destroying myks\n")
    }
  }
}
