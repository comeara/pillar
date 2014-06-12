package com.chrisomeara.pillar

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

class ReportingMigratorSpec extends FunSpec with ShouldMatchers with MockitoSugar {
  val reporter = mock[Reporter]
  val wrapped = mock[Migrator]
  val migrator = new ReportingMigrator(reporter, wrapped)
  val dataStore = DataStore("faker", "pillar_test", "127.0.0.1")

  describe("#initialize") {
    val replicationOptions = mock[ReplicationOptions]
    migrator.initialize(dataStore, replicationOptions)

    it("reports the initialize action") {
      verify(reporter).initializing(dataStore, replicationOptions)
    }

    it("delegates to the wrapped migrator") {
      verify(wrapped).initialize(dataStore, replicationOptions)
    }
  }

  describe("#migrate") {
    migrator.migrate(dataStore)

    it("reports the migrate action") {
      verify(reporter).migrating(dataStore, None)
    }

    it("delegates to the wrapped migrator") {
      verify(wrapped).migrate(dataStore, None)
    }
  }

  describe("#destroy") {
    migrator.destroy(dataStore)

    it("reports the destroy action") {
      verify(reporter).destroying(dataStore)
    }

    it("delegates to the wrapped migrator") {
      verify(wrapped).destroy(dataStore)
    }
  }
}
