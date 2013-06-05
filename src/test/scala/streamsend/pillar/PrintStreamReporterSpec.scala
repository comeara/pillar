package streamsend.pillar

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.io.{ByteArrayOutputStream, PrintStream}

class PrintStreamReporterSpec extends FunSpec with ShouldMatchers with OneInstancePerTest {
  val dataStore = DataStore("faker", "pillar_test", "127.0.0.1")
  val output = new ByteArrayOutputStream()
  val stream = new PrintStream(output)
  val reporter = new PrintStreamReporter(stream)

  describe("#initializing") {
    it("should print to the stream") {
      reporter.initializing(dataStore, ReplicationOptions.default)
      output.toString should equal(s"Initializing ${dataStore.name} data store\n")
    }
  }

  describe("#migrating") {
    describe("without date restriction") {
      it("should print to the stream") {
        reporter.migrating(dataStore, None)
        output.toString should equal(s"Migrating ${dataStore.name} data store\n")
      }
    }
  }
}
