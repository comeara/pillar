package streamsend.pillar

import com.datastax.driver.core.{Session, Cluster}
import com.datastax.driver.core.exceptions.AlreadyExistsException

object Migrator {
  def apply(seedAddress: String = "127.0.0.1"): Migrator = {
    new Migrator(seedAddress)
  }
}

class Migrator(seedAddress: String) {
  private val cluster = Cluster.builder().addContactPoint(seedAddress).build()

  def up(keyspaceName: String, migrations: Seq[Migration]) {

  }

  def initialize(keyspaceName: String, replicationOptions: Map[String, Any] = ReplicationOptions.default) {
    val session = cluster.connect()
    val replicationPart = serializeOptionMap(replicationOptions)
    executeIdempotentCommand(session, "CREATE KEYSPACE %s WITH replication = %s".format(keyspaceName, replicationPart))
    executeIdempotentCommand(session, "CREATE TABLE %s.schema_versions (id TEXT PRIMARY KEY, applied_at TIMESTAMP)".format(keyspaceName))
  }

  private def serializeOptionMap(options: Map[String, Any]): String = {
    "{" + options.map { case(key, value) =>
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