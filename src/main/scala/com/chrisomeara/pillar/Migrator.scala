package com.chrisomeara.pillar

import java.util.Date

import com.datastax.driver.core.Session

object Migrator {
  def apply(registry: Registry): Migrator = {
    new CassandraMigrator(registry)
  }

  def apply(registry: Registry, reporter: Reporter): Migrator = {
    new ReportingMigrator(reporter, apply(registry))
  }
}

trait Migrator {
  def migrate(session: Session, dateRestriction: Option[Date] = None)

  def initialize(session: Session, keyspace: String, replicationOptions: ReplicationOptions = ReplicationOptions.default)

  def destroy(session: Session, keyspace: String)
}