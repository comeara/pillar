package com.chrisomeara.pillar

import com.datastax.driver.core.exceptions.InvalidQueryException
import org.scalatest.{BeforeAndAfter, FeatureSpec, GivenWhenThen}
import org.scalatest.matchers.ShouldMatchers
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.chrisomeara.pillar.cli.App

class PillarCommandLineAcceptanceSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfter with ShouldMatchers with AcceptanceAssertions {
  val seedAddress = sys.env.getOrElse("PILLAR_SEED_ADDRESS", "127.0.0.1")
  val cluster = Cluster.builder().addContactPoint(seedAddress).build()
  val session = cluster.connect()
  val keyspaceName = "pillar_acceptance_test"

  before {
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
      App().run(Array("-e", "acceptance_test", "initialize", "faker"))

      Then("the keyspace contains a applied_migrations column family")
      assertEmptyAppliedMigrationsTable()
    }
  }

  feature("The operator can apply migrations") {
    info("As an application operator")
    info("I want to migrate a Cassandra keyspace from an older version of the schema to a newer version")
    info("So that I can run an application using the schema")

    scenario("all migrations") {
      Given("an initialized, empty, keyspace")
      App().run(Array("-e", "acceptance_test", "initialize", "faker"))

      Given("a migration that creates an events table")
      Given("a migration that creates a views table")

      When("the migrator migrates the schema")
      App().run(Array("-e", "acceptance_test", "-d", "src/test/resources/pillar/migrations", "migrate", "faker"))

      Then("the keyspace contains the events table")
      session.execute(QueryBuilder.select().from(keyspaceName, "events")).all().size() should equal(0)

      And("the keyspace contains the views table")
      session.execute(QueryBuilder.select().from(keyspaceName, "views")).all().size() should equal(0)

      And("the applied_migrations table records the migrations")
      session.execute(QueryBuilder.select().from(keyspaceName, "applied_migrations")).all().size() should equal(3)

      And("the first migration was authored at Fri May 31 18:01:02 2013")
      session.execute(QueryBuilder.select().from(keyspaceName, "applied_migrations").where(QueryBuilder.eq("authored_at", 1370023262000L))).all().size() should equal(1)
    }
  }
}
