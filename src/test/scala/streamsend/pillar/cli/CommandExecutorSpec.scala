package streamsend.pillar.cli

import org.scalatest.{BeforeAndAfter, FunSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import streamsend.pillar.{Reporter, DataStore, Migrator, Registry}
import java.util.Date

class CommandExecutorSpec extends FunSpec with BeforeAndAfter with ShouldMatchers with MockitoSugar {
  describe("#execute") {
    val dataStore = new DataStore("faker", "keyspace", "seedAddress")
    val registry = mock[Registry]
    val reporter = mock[Reporter]
    val migrator = mock[Migrator]
    val migratorConstructor = mock[((DataStore, Registry, Reporter) => Migrator)]
    stub(migratorConstructor.apply(dataStore, registry, reporter)).toReturn(migrator)
    val executor = new CommandExecutor()(migratorConstructor)

    describe("an initialize action") {
      val command = Command(Initialize, dataStore, None, registry)

      executor.execute(command, reporter)

      it("initializes") {
        verify(migrator).initialize(dataStore)
      }
    }

    describe("a migrate action without date restriction") {
      val command = Command(Migrate, dataStore, None, registry)

      executor.execute(command, reporter)

      it("migrates") {
        verify(migrator).migrate(dataStore, None)
      }
    }

    describe("a migrate action with date restriction") {
      val date = new Date()
      val command = Command(Migrate, dataStore, Some(date.getTime), registry)

      executor.execute(command, reporter)

      it("migrates") {
        verify(migrator).migrate(dataStore, Some(date))
      }
    }
  }
}