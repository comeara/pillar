package streamsend.pillar

import com.datastax.driver.core.{Session, Cluster}
import com.datastax.driver.core.exceptions.AlreadyExistsException

object ReplicationOptions {
  val default = Map("class" -> "SimpleStrategy", "replication_factor" -> 3)
}

object Migrator {
  private val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()

  def up(keyspaceName: String, migrations: Seq[Migration]) {

  }

  def initialize(keyspaceName: String, replicationOptions: Map[String, Any] = ReplicationOptions.default) {
    val session = cluster.connect()
    val replicationPart = "{" + replicationOptions.map { case(key, value) =>
      value match {
        case number: Int => "'%s':%d".format(key, number)
        case string: String => "'%s':'%s'".format(key, string)
      }
    }.mkString(",") + "}"
    executeIdempotentCommand(session, "CREATE KEYSPACE %s WITH replication = %s".format(keyspaceName, replicationPart))
    executeIdempotentCommand(session, "CREATE TABLE %s.schema_versions (id TEXT PRIMARY KEY, applied_at TIMESTAMP)".format(keyspaceName))
  }

  private def executeIdempotentCommand(session: Session, statement: String) {
    try {
      session.execute(statement)
    } catch {
      case ok: AlreadyExistsException =>
    }
  }
}