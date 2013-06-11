package com.streamsend.pillar.cli

import com.typesafe.config.Config
import java.io.File
import com.streamsend.pillar.{Reporter, DataStore, Registry}

object Command {
  def buildFromConfiguration(commandLineConfiguration: CommandLineConfiguration, applicationConfiguration: Config)(implicit registryConstructor: ((File, Reporter) => Registry), reporter: Reporter): Command = {
    val dataStore = DataStore.fromConfiguration(commandLineConfiguration.dataStore, commandLineConfiguration.environment, applicationConfiguration)
    val registry = registryConstructor(new File(commandLineConfiguration.migrationsDirectory, dataStore.name), reporter)
    new Command(commandLineConfiguration.command,
      dataStore,
      commandLineConfiguration.timeStampOption,
      registry
    )
  }
}

case class Command(action: MigratorAction, dataStore: DataStore, timeStampOption: Option[Long], registry: Registry)