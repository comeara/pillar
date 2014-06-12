package com.chrisomeara.pillar

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.io.{ByteArrayOutputStream, PrintStream}
import java.util.Date

class PrintStreamReporterSpec extends FunSpec with ShouldMatchers with OneInstancePerTest {
  val dataStore = DataStore("faker", "pillar_test", "127.0.0.1")
  val migration = Migration("creates things table", new Date(1370489972546L), "up", Some("down"))
  val output = new ByteArrayOutputStream()
  val stream = new PrintStream(output)
  val reporter = new PrintStreamReporter(stream)

  describe("#initializing") {
    it("should print to the stream") {
      reporter.initializing(dataStore, ReplicationOptions.default)
      output.toString should equal("Initializing faker data store\n")
    }
  }

  describe("#migrating") {
    describe("without date restriction") {
      it("should print to the stream") {
        reporter.migrating(dataStore, None)
        output.toString should equal("Migrating faker data store\n")
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
      reporter.destroying(dataStore)
      output.toString should equal("Destroying faker data store\n")
    }
  }
}
