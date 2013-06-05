package streamsend.pillar.cli

import com.typesafe.config.Config
import java.io.File
import streamsend.pillar.{DataStore, MigrationRegistry}

object PillarCommand {
  def buildFromConfiguration(commandLineConfiguration: CommandLineConfiguration, applicationConfiguration: Config)(implicit registryConstructor: ((File) => MigrationRegistry)): PillarCommand = {
    val dataStore = DataStore.fromConfiguration(commandLineConfiguration.dataStore, commandLineConfiguration.environment, applicationConfiguration)
    val registry = registryConstructor(commandLineConfiguration.migrationsDirectory)
    new PillarCommand(commandLineConfiguration.command,
      dataStore,
      commandLineConfiguration.timeStampOption,
      registry
    )
  }
}

case class PillarCommand(action: MigratorAction, dataStore: DataStore, timeStampOption: Option[Long], registry: MigrationRegistry)