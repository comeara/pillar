package com.chrisomeara.pillar

import java.io.PrintStream
import java.util.Date

import com.datastax.driver.core.Session

class PrintStreamReporter(stream: PrintStream) extends Reporter {
  override def initializing(session: Session, keyspace: String, replicationOptions: ReplicationOptions) {
    stream.println(s"Initializing $keyspace")
  }

  override def migrating(session: Session, dateRestriction: Option[Date]) {
    stream.println(s"Migrating with date restriction $dateRestriction")
  }

  override def applying(migration: Migration) {
    stream.println(s"Applying ${migration.authoredAt.getTime}: ${migration.description}")
  }

  override def reversing(migration: Migration) {
    stream.println(s"Reversing ${migration.authoredAt.getTime}: ${migration.description}")
  }

  override def destroying(session: Session, keyspace: String) {
    stream.println(s"Destroying $keyspace")
  }
}