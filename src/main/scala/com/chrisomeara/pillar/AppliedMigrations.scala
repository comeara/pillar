package com.chrisomeara.pillar

import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import scala.collection.JavaConversions
import java.util.Date

object AppliedMigrations {
  def apply(session: Session, registry: Registry): AppliedMigrations = {
    val results = session.execute(QueryBuilder.select("authored_at", "description").from("applied_migrations"))
    new AppliedMigrations(JavaConversions.asScalaBuffer(results.all()).map {
      row => registry(MigrationKey(row.getTimestamp("authored_at"), row.getString("description")))
    })
  }
}

class AppliedMigrations(applied: Seq[Migration]) {
  def length: Int = applied.length

  def apply(index: Int): Migration = applied.apply(index)

  def iterator: Iterator[Migration] = applied.iterator

  def authoredAfter(date: Date): Seq[Migration] = applied.filter(migration => migration.authoredAfter(date))

  def contains(other: Migration): Boolean = applied.contains(other)
}