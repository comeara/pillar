package streamsend.pillar

import org.scalatest.{FunSpec, BeforeAndAfter}
import org.scalatest.matchers.ShouldMatchers
import com.datastax.driver.core.{Session, Cluster}
import com.datastax.driver.core.querybuilder.QueryBuilder
import java.util.Date
import com.datastax.driver.core.exceptions.InvalidQueryException

class AppliedMigrationsSpec extends FunSpec with BeforeAndAfter with ShouldMatchers {
  val keyspaceName = "test_%d".format(System.currentTimeMillis())
  val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
  var session: Session = _
  val migrationKey: MigrationKey = MigrationKey(new Date(), "tests things")

  before {
    Migrator().initialize(keyspaceName, Map("class" -> "SimpleStrategy", "replication_factor" -> 1))
    session = cluster.connect(keyspaceName)
    session.execute(QueryBuilder.
      insertInto("applied_migrations").
      value("authored_at", migrationKey.authoredAt).
      value("description", migrationKey.description).
      value("applied_at", System.currentTimeMillis())
    )
  }

  after {
    try {
      session.execute("DROP KEYSPACE %s".format(keyspaceName))
    } catch {
      case ok: InvalidQueryException =>
    }
  }

  describe("#contains") {
    describe("a collection with one applied migration") {
      it("returns true for the key of the applied migration") {
        val migrations = AppliedMigrations(session)
        migrations.contains(migrationKey) should be(true)
      }
    }
  }
}
