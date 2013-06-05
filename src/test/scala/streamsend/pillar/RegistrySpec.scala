package streamsend.pillar

import org.scalatest.{FunSpec, BeforeAndAfter}
import org.scalatest.matchers.ShouldMatchers
import java.io.File

class RegistrySpec extends FunSpec with BeforeAndAfter with ShouldMatchers {
  describe(".apply") {
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
}
