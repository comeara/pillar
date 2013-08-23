package com.streamsend.pillar

import java.util.Date

object Migrator {
  def apply(registry: Registry): Migrator = {
    new CassandraMigrator(registry)
  }

  def apply(registry: Registry, reporter: Reporter): Migrator = {
    new ReportingMigrator(reporter, apply(registry))
  }
}

trait Migrator {
  def migrate(dataStore: DataStore, dateRestriction: Option[Date] = None)

  def initialize(dataStore: DataStore, replicationOptions: ReplicationOptions = ReplicationOptions.default)

  def destroy(dataStore: DataStore)
}