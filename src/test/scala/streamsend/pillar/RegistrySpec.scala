package streamsend.pillar

import org.scalatest.{FunSpec, BeforeAndAfter}
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

class RegistrySpec extends FunSpec with BeforeAndAfter with ShouldMatchers with MockitoSugar {
  describe(".fromDirectory") {
    describe("without a reporter parameter") {
      describe("with a directory that exists and has migration files") {
        it("returns a registry with migrations") {
          val registry = Registry.fromDirectory(new File("src/test/resources/pillar/migrations/faker/"))
          registry.all.size should equal(3)
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
}
