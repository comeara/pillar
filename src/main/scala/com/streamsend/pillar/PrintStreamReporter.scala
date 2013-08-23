package com.streamsend.pillar

import java.io.PrintStream
import java.util.Date

class PrintStreamReporter(stream: PrintStream) extends com.streamsend.pillar.Reporter {
  def initializing(dataStore: DataStore, replicationOptions: ReplicationOptions) {
    stream.println(s"Initializing ${dataStore.name} data store")
  }

  def migrating(dataStore: DataStore, dateRestriction: Option[Date]) {
    stream.println(s"Migrating ${dataStore.name} data store")
  }

  def applying(migration: Migration) {
    stream.println(s"Applying ${migration.authoredAt.getTime}: ${migration.description}")
  }

  def reversing(migration: Migration) {
    stream.println(s"Reversing ${migration.authoredAt.getTime}: ${migration.description}")
  }

  def destroying(dataStore: DataStore) {
    stream.println(s"Destroying ${dataStore.name} data store")
  }
}