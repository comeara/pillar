package streamsend.pillar

import com.datastax.driver.core.{Session, Cluster}
import com.datastax.driver.core.querybuilder.QueryBuilder
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

    val toReverse: Seq[Migration] = (dateRestriction match {
      case None => List.empty[Migration]
      case Some(cutOff) => appliedMigrations.authoredAfter(cutOff)
    }).sortBy(_.authoredAt).reverse

    toReverse.foreach {
      case reversible: ReversibleMigrationWithNoopDown =>
        deleteFromAppliedMigrations(session, reversible)
      case reversible: ReversibleMigration =>
        session.execute(reversible.down)
        deleteFromAppliedMigrations(session, reversible)
      case irreversible: IrreversibleMigration =>
        throw new IrreversibleMigrationException(irreversible)
    }

    val toApply: Seq[Migration] = (dateRestriction match {
      case None => registry.all
      case Some(cutOff) => registry.authoredBefore(cutOff)
    }).filter(migration => !appliedMigrations.contains(migration))

    toApply.foreach {
      migration =>
        session.execute(migration.up)
        session.execute(QueryBuilder.
          insertInto("applied_migrations").
          value("authored_at", migration.authoredAt).
          value("description", migration.description).
          value("applied_at", System.currentTimeMillis())
        )
    }
  }

  def initialize(keyspaceName: String, replicationOptions: Map[String, Any] = ReplicationOptions.default) {
    val session = cluster.connect()
    executeIdempotentCommand(session, "CREATE KEYSPACE %s WITH replication = %s".format(keyspaceName, serializeOptionMap(replicationOptions)))
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

  private def deleteFromAppliedMigrations(session: Session, migration: Migration) {
    session.execute(QueryBuilder.
      delete().
      from("applied_migrations").
      where(QueryBuilder.eq("authored_at", migration.authoredAt)).
      and(QueryBuilder.eq("description", migration.description))
    )
  }

  private def executeIdempotentCommand(session: Session, statement: String) {
    try {
      session.execute(statement)
    } catch {
      case _: AlreadyExistsException =>
    }
  }

  private def serializeOptionMap(options: Map[String, Any]): String = {
    "{" + options.map {
      case (key, value) =>
        value match {
          case number: Int => "'%s':%d".format(key, number)
          case string: String => "'%s':'%s'".format(key, string)
        }
    }.mkString(",") + "}"
  }
}