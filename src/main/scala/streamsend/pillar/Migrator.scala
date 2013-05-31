package streamsend.pillar

import com.datastax.driver.core.{Session, Cluster}
import java.util.Date
import com.datastax.driver.core.exceptions.AlreadyExistsException

object Migrator {
  def apply(keyspaceName: String, registry: MigrationRegistry, seedAddress: String = "127.0.0.1"): Migrator = {
    new Migrator(keyspaceName, registry, seedAddress)
  }
}

class Migrator(keyspaceName: String, registry: MigrationRegistry, seedAddress: String) {
  private val cluster = Cluster.builder().addContactPoint(seedAddress).build()

  def migrate(dateRestriction: Option[Date] = None) {
    val session = cluster.connect(keyspaceName)
    val appliedMigrations = AppliedMigrations(session, registry)

    selectMigrationsToReverse(dateRestriction, appliedMigrations).foreach(_.executeDownStatement(session))
    selectMigrationsToApply(dateRestriction, appliedMigrations).foreach(_.executeUpStatement(session))
  }

  def initialize(keyspaceName: String, replicationOptions: ReplicationOptions = ReplicationOptions.default) {
    val session = cluster.connect()
    executeIdempotentCommand(session, "CREATE KEYSPACE %s WITH replication = %s".format(keyspaceName, replicationOptions.toString()))
    executeIdempotentCommand(session,
      """
        | CREATE TABLE %s.applied_migrations (
        |   authored_at timestamp,
        |   description text,
        |   applied_at timestamp,
        |   PRIMARY KEY (authored_at, description)
        |  )
      """.stripMargin.format(keyspaceName)
    )
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
    }).filter(migration => !appliedMigrations.contains(migration))
  }

  private def selectMigrationsToReverse(dateRestriction: Option[Date], appliedMigrations: AppliedMigrations): Seq[Migration] = {
    (dateRestriction match {
      case None => List.empty[Migration]
      case Some(cutOff) => appliedMigrations.authoredAfter(cutOff)
    }).sortBy(_.authoredAt).reverse
  }
}