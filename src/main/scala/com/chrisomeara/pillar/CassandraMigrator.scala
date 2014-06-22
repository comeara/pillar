package com.chrisomeara.pillar

import java.util.Date

import com.datastax.driver.core.Session
import com.datastax.driver.core.exceptions.AlreadyExistsException

class CassandraMigrator(registry: Registry) extends Migrator {
  override def migrate(session: Session, dateRestriction: Option[Date] = None) {
    val appliedMigrations = AppliedMigrations(session, registry)
    selectMigrationsToReverse(dateRestriction, appliedMigrations).foreach(_.executeDownStatement(session))
    selectMigrationsToApply(dateRestriction, appliedMigrations).foreach(_.executeUpStatement(session))
  }

  override def initialize(session: Session, keyspace: String, replicationOptions: ReplicationOptions = ReplicationOptions.default) {
    executeIdempotentCommand(session, "CREATE KEYSPACE %s WITH replication = %s".format(keyspace, replicationOptions.toString()))
    executeIdempotentCommand(session,
      """
        | CREATE TABLE %s.applied_migrations (
        |   authored_at timestamp,
        |   description text,
        |   applied_at timestamp,
        |   PRIMARY KEY (authored_at, description)
        |  )
      """.stripMargin.format(keyspace)
    )
  }

  override def destroy(session: Session, keyspace: String) {
    session.execute("DROP KEYSPACE %s".format(keyspace))
  }

  private def executeIdempotentCommand(session: Session, statement: String) {
    try {
      session.execute(statement)
    } catch {
      case _: AlreadyExistsException =>
    }
  }

  private def selectMigrationsToApply(dateRestriction: Option[Date], appliedMigrations: AppliedMigrations): Seq[Migration] = {
    (dateRestriction match {
      case None => registry.all
      case Some(cutOff) => registry.authoredBefore(cutOff)
    }).filter(!appliedMigrations.contains(_))
  }

  private def selectMigrationsToReverse(dateRestriction: Option[Date], appliedMigrations: AppliedMigrations): Seq[Migration] = {
    (dateRestriction match {
      case None => List.empty[Migration]
      case Some(cutOff) => appliedMigrations.authoredAfter(cutOff)
    }).sortBy(_.authoredAt).reverse
  }
}