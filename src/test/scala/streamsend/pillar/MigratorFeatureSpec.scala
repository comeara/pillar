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
      Given("a non-existent keyspace")

      When("the migrator initializes the keyspace")
      Migrator().initialize(keyspaceName)

      Then("the keyspace contains a applied_migrations column family")
      val result = session.execute(QueryBuilder.select().from(keyspaceName, "applied_migrations"))
      result.all().size() should equal(0)
    }

    scenario("initialize an existing keyspace without a applied_migrations column family") {
      Given("an existing keyspace")
      session.execute("CREATE KEYSPACE %s WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1}".format(keyspaceName))

      When("the migrator initializes the keyspace")
      Migrator().initialize(keyspaceName)

      Then("the keyspace contains a applied_migrations column family")
      val result = session.execute(QueryBuilder.select().from(keyspaceName, "applied_migrations"))
      result.all().size() should equal(0)
    }

    scenario("initialize an existing keyspace with a applied_migrations column family") {
      Given("an existing keyspace")
      session.execute("CREATE KEYSPACE %s WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1}".format(keyspaceName))
      session.execute("CREATE TABLE %s.applied_migrations (id TEXT PRIMARY KEY, applied_at TIMESTAMP)".format(keyspaceName))

      When("the migrator initializes the keyspace")
      Migrator().initialize(keyspaceName)

      Then("the keyspace contains a applied_migrations column family")
      val result = session.execute(QueryBuilder.select().from(keyspaceName, "applied_migrations"))
      result.all().size() should equal(0)
    }
  }

  feature("The operator can generate an empty migration") {}

  feature("The operator can migrate up") {
    info("As an application operator")
    info("I want to migrate a Cassandra keyspace from an older version of the schema to a newer version")
    info("So that I can run an application using the schema")

    scenario("apply one migration") {
      val migrator = Migrator()
      Given("an initialized keyspace")
      migrator.initialize(keyspaceName)

      Given("a migration that creates an events table")
      val migration = Migration("%d_create_events_table".format(System.currentTimeMillis()),
        """
          |CREATE TABLE events (
          |  batch_id text,
          |  occurred_at timestamp,
          |  event_type text,
          |  payload blob,
          |  PRIMARY KEY (batch_id, occurred_at, event_type)
          |)
        """.stripMargin)

      When("the migrator migrates up")
      migrator.up(keyspaceName, Seq(migration))

      Then("the keyspace contains the events table")
      session.execute(QueryBuilder.select().from(keyspaceName, "events")).all().size() should equal(0)

      And("the applied_migrations table records the migration")
      session.execute(QueryBuilder.select().from(keyspaceName, "applied_migrations")).all().size() should equal(1)
    }
  }

  feature("The operator can migrate down") {}

  feature("The operator can list applied migrations") {}
}