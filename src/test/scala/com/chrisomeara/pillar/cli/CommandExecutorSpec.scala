package com.chrisomeara.pillar.cli

import java.util.Date

import com.chrisomeara.pillar.{Migrator, Registry, Reporter}
import com.datastax.driver.core.Session
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSpec}

class CommandExecutorSpec extends FunSpec with BeforeAndAfter with MockitoSugar {
  describe("#execute") {
    val session = mock[Session]
    val keyspace = "myks"
    val registry = mock[Registry]
    val reporter = mock[Reporter]
    val migrator = mock[Migrator]
    val migratorConstructor = mock[((Registry, Reporter) => Migrator)]
    stub(migratorConstructor.apply(registry, reporter)).toReturn(migrator)
    val executor = new CommandExecutor()(migratorConstructor)

    describe("an initialize action") {
      val command = Command(Initialize, session, keyspace, None, registry)

      executor.execute(command, reporter)

      it("initializes") {
        verify(migrator).initialize(session, keyspace)
      }
    }

    describe("a migrate action without date restriction") {
      val command = Command(Migrate, session, keyspace, None, registry)

      executor.execute(command, reporter)

      it("migrates") {
        verify(migrator).migrate(session, None)
      }
    }

    describe("a migrate action with date restriction") {
      val date = new Date()
      val command = Command(Migrate, session, keyspace, Some(date.getTime), registry)

      executor.execute(command, reporter)

      it("migrates") {
        verify(migrator).migrate(session, Some(date))
      }
    }
  }
}