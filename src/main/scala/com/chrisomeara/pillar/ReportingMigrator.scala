package com.chrisomeara.pillar

import java.util.Date

class ReportingMigrator(reporter: Reporter, wrapped: Migrator) extends Migrator {
  def initialize(dataStore: DataStore, replicationOptions: ReplicationOptions) {
    reporter.initializing(dataStore, replicationOptions)
    wrapped.initialize(dataStore, replicationOptions)
  }

  def migrate(dataStore: DataStore, dateRestriction: Option[Date]) {
    reporter.migrating(dataStore, dateRestriction)
    wrapped.migrate(dataStore, dateRestriction)
  }

  def destroy(dataStore: DataStore) {
    reporter.destroying(dataStore)
    wrapped.destroy(dataStore)
  }
}