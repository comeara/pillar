package streamsend.pillar.cli

import org.scalatest.{BeforeAndAfter, FunSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import streamsend.pillar.{DataStore, Migrator, MigrationRegistry}
import java.util.Date

class PillarCommandExecutorSpec extends FunSpec with BeforeAndAfter with ShouldMatchers with MockitoSugar {
  describe("#execute") {
    val dataStore = new DataStore("faker", "keyspace", "seedAddress")
    val registry = mock[MigrationRegistry]
    val migrator = mock[Migrator]
    val migratorConstructor = mock[((DataStore, MigrationRegistry) => Migrator)]
    stub(migratorConstructor.apply(dataStore, registry)).toReturn(migrator)
    val executor = new PillarCommandExecutor()(migratorConstructor)

    describe("an initialize action") {
      val command = PillarCommand(Initialize, dataStore, None, registry)

      executor.execute(command)

      it("initializes") {
        verify(migrator).initialize()
      }
    }

    describe("a migrate action without date restriction") {
      val command = PillarCommand(Migrate, dataStore, None, registry)

      executor.execute(command)

      it("migrates") {
        verify(migrator).migrate(None)
      }
    }

    describe("a migrate action with date restriction") {
      val date = new Date()
      val command = PillarCommand(Migrate, dataStore, Some(date.getTime), registry)

      executor.execute(command)

      it("migrates") {
        verify(migrator).migrate(Some(date))
      }
    }
  }
}