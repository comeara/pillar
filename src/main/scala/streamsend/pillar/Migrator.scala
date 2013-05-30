package streamsend.pillar

import com.datastax.driver.core.{Session, Cluster}
import com.datastax.driver.core.querybuilder.QueryBuilder
import scala.collection.JavaConversions
import java.util.Date
import com.datastax.driver.core.exceptions.AlreadyExistsException

object Migrator {
  def apply(seedAddress: String = "127.0.0.1"): Migrator = {
    new Migrator(seedAddress)
  }
}

class Migrator(seedAddress: String) {
  private val cluster = Cluster.builder().addContactPoint(seedAddress).build()

  def apply(keyspaceName: String, migrations: Seq[Migration], asOf: Option[Date] = None) {
    val session = cluster.connect(keyspaceName)
    val allMigrations = migrations.foldLeft(Map.empty[(Date, String), Migration]) {
      (memo, migration) => memo + ((migration.authoredAt, migration.description) -> migration)
    }
    val results = session.execute(QueryBuilder.select("authored_at", "description").from("applied_migrations"))
    val appliedMigrations = JavaConversions.asScalaBuffer(results.all()).map {
      row => (row.getDate("authored_at"), row.getString("description"))
    }
    val toReverse: Seq[Migration] = asOf match {
      case None => List.empty[Migration]
      case Some(cutOff) =>
        appliedMigrations.filter { case (authoredAt, _) => authoredAt.after(cutOff) }.map { case (authoredAt, description) => allMigrations(authoredAt, description) }
    }

    toReverse.sortBy(_.authoredAt).reverseIterator.foreach {
      case reversible: ReversibleMigrationWithNoopDown =>
        deleteFromAppliedMigrations(session, reversible)
      case reversible: ReversibleMigration =>
        session.execute(reversible.down)
        deleteFromAppliedMigrations(session, reversible)
    }

    val toApply: Seq[Migration] = asOf match {
      case None => migrations
      case Some(cutOff) =>
        migrations.filter { migration => migration.authoredAt.compareTo(cutOff) <= 0 }
    }

    toApply.foreach {
      migration =>
        if (!appliedMigrations.contains(migration.authoredAt, migration.description)) {
          session.execute(migration.up)
          session.execute(QueryBuilder.
            insertInto("applied_migrations").
            value("authored_at", migration.authoredAt).
            value("description", migration.description).
            value("applied_at", System.currentTimeMillis())
          )
        }
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