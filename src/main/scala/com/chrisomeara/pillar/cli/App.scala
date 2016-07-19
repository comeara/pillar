package com.chrisomeara.pillar.cli

import java.io.File

import com.chrisomeara.pillar.{ConfigurationException, PrintStreamReporter, Registry, Reporter}
import com.datastax.driver.core.Cluster
import com.typesafe.config.{Config, ConfigFactory}

object App {
  def apply(reporter: Reporter = new PrintStreamReporter(System.out)): App = {
    new App(reporter)
  }

  def main(arguments: Array[String]) {
    try {
      App().run(arguments)
    } catch {
      case exception: Exception =>
        System.err.println(exception.getMessage)
        System.exit(1)
    }

    System.exit(0)
  }
}

class App(reporter: Reporter) {
  def run(arguments: Array[String]) {
    val commandLineConfiguration = CommandLineConfiguration.buildFromArguments(arguments)
    val registry = Registry.fromDirectory(new File(commandLineConfiguration.migrationsDirectory, commandLineConfiguration.dataStore))
    val configuration = ConfigFactory.load()
    val dataStoreName = commandLineConfiguration.dataStore
    val environment = commandLineConfiguration.environment
    val keyspace = getFromConfiguration(configuration, dataStoreName, environment, "cassandra-keyspace-name")
    val seedAddress = getFromConfiguration(configuration, dataStoreName, environment, "cassandra-seed-address", Some("127.0.0.1"))
    val port = Integer.valueOf(getFromConfiguration(configuration, dataStoreName, environment, "cassandra-port", Some(9042.toString)))
    val username = getFromConfiguration(configuration, dataStoreName, environment, "cassandra-username", Some("cassandra"))
    val password = getFromConfiguration(configuration, dataStoreName, environment, "cassandra-password", Some("cassandra"))
    val builder = Cluster.builder().addContactPoint(seedAddress).withPort(port).withCredentials(username, password)
    if (getFromConfiguration(configuration, dataStoreName, environment, "cassandra-ssl", Some("false")).toBoolean) {
      builder.withSSL()
    }
    val cluster = builder.build()
    val session = commandLineConfiguration.command match {
      case Initialize => cluster.connect()
      case _ => cluster.connect(keyspace)
    }
    val command = Command(commandLineConfiguration.command, session, keyspace, commandLineConfiguration.timeStampOption, registry)

    try {
      CommandExecutor().execute(command, reporter)
    } finally {
      session.close()
    }
  }

  private def getFromConfiguration(configuration: Config, name: String, environment: String, key: String, default: Option[String] = None): String = {
    val path = s"pillar.$name.$environment.$key"
    if (configuration.hasPath(path)) return configuration.getString(path)
    if (default.eq(None)) throw new ConfigurationException(s"$path not found in application configuration")
    default.get
  }
}