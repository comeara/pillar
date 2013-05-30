package streamsend.pillar

import org.scalatest.{BeforeAndAfter, GivenWhenThen, FeatureSpec}
import org.scalatest.matchers.ShouldMatchers
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.exceptions.InvalidQueryException

class MigratorFeatureSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfter with ShouldMatchers {
  val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
  val session = cluster.connect()
  val keyspaceName = "test_%d".format(System.currentTimeMillis())

  after {
    try {
      session.execute("DROP KEYSPACE %s".format(keyspaceName))
    } catch {
      case ok: InvalidQueryException =>
    }
  }

  feature("The operator can initialize a keyspace") {
    info("As an application operator")
    info("I want to initialize a Cassandra keyspace")
    info("So that I can manage the keyspace schema")

    scenario("initialize a non-existent keyspace") {
      given("a non-existent keyspace")

      when("the migrator initializes the keyspace")
      Migrator.initialize(keyspaceName)

      then("the keyspace contains a schema_versions column family")
      val result = session.execute(QueryBuilder.select().from(keyspaceName, "schema_versions"))
      result.all().size() should equal(0)
    }

    scenario("initialize an existing keyspace without a schema_versions column family") {
      given("an existing keyspace")
      session.execute("CREATE KEYSPACE %s WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1}".format(keyspaceName))

      when("the migrator initializes the keyspace")
      Migrator.initialize(keyspaceName)

      then("the keyspace contains a schema_versions column family")
      val result = session.execute(QueryBuilder.select().from(keyspaceName, "schema_versions"))
      result.all().size() should equal(0)
    }

    scenario("initialize an existing keyspace with a schema_versions column family") {
      given("an existing keyspace")
      session.execute("CREATE KEYSPACE %s WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1}".format(keyspaceName))
      session.execute("CREATE TABLE %s.schema_versions (id TEXT PRIMARY KEY, applied_at TIMESTAMP)".format(keyspaceName))

      when("the migrator initializes the keyspace")
      Migrator.initialize(keyspaceName)

      then("the keyspace contains a schema_versions column family")
      val result = session.execute(QueryBuilder.select().from(keyspaceName, "schema_versions"))
      result.all().size() should equal(0)
    }
  }

  feature("The operator can generate an empty migration") {}

  feature("The operator can migrate up") {
    info("As an application operator")
    info("I want to migrate a Cassandra keyspace from an older version of the schema to a newer version")
    info("So that I can run an application using the schema")

    scenario("migrate up with zero migrations") {
    }
  }

  feature("The operator can migrate down") {}

  feature("The operator can list applied migrations") {}
}
