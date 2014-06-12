package com.chrisomeara.pillar

import com.typesafe.config.Config

object DataStore {
  def fromConfiguration(name: String, environment: String, configuration: Config): DataStore = {
    new DataStore(
      name,
      getFromConfiguration(configuration, name, environment, "cassandra-keyspace-name"),
      getFromConfiguration(configuration, name, environment, "cassandra-seed-address")
    )
  }

  private def getFromConfiguration(configuration: Config, name: String, environment: String, key: String): String = {
    val path = s"pillar.$name.$environment.$key"
    if (!configuration.hasPath(path)) throw new ConfigurationException(s"$path not found in application configuration")
    configuration.getString(path)
  }
}

case class DataStore(name: String, keyspace: String, seedAddress: String)