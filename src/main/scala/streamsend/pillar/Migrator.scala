package streamsend.pillar

import com.datastax.driver.core.{Session, Cluster}
import com.datastax.driver.core.exceptions.AlreadyExistsException
import com.datastax.driver.core.querybuilder.QueryBuilder
import scala.collection.JavaConversions
import java.util.Date

object Migrator {
  def apply(seedAddress: String = "127.0.0.1"): Migrator = {
    new Migrator(seedAddress)
  }
}

class Migrator(seedAddress: String) {
  private val cluster = Cluster.builder().addContactPoint(seedAddress).build()

  def up(keyspaceName: String, migrations: Seq[Migration]) {
    val session = cluster.connect(keyspaceName)
    val results = session.execute(QueryBuilder.select("authored_at", "description").from("applied_migrations"))
    val appliedMigrations = JavaConversions.asScalaIterator(results.iterator()).foldLeft(Map.empty[(Date, String), Null]) {
      (memo, row) => memo + ((row.getDate("authored_at"), row.getString("description")) -> null)
    }
    migrations.foreach {
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

  private def serializeOptionMap(options: Map[String, Any]): String = {
    "{" + options.map {
      case (key, value) =>
        value match {
          case number: Int => "'%s':%d".format(key, number)
          case string: String => "'%s':'%s'".format(key, string)
        }
    }.mkString(",") + "}"
  }

  private def executeIdempotentCommand(session: Session, statement: String) {
    try {
      session.execute(statement)
    } catch {
      case ok: AlreadyExistsException =>
    }
  }
}