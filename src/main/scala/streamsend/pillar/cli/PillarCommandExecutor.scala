package streamsend.pillar.cli

import java.util.Date
import streamsend.pillar.{DataStore, MigrationRegistry, Migrator}

object PillarCommandExecutor {
  implicit private val migratorConstructor: ((DataStore, MigrationRegistry) => Migrator) = Migrator.apply

  def apply(): PillarCommandExecutor = new PillarCommandExecutor()
}

class PillarCommandExecutor(implicit val migratorConstructor: ((DataStore, MigrationRegistry) => Migrator)) {
  def execute(command: PillarCommand) {
    val migrator = migratorConstructor(command.dataStore, command.registry)

    command.action match {
      case Initialize => migrator.initialize()
      case Migrate    => migrator.migrate(command.timeStampOption.map(new Date(_)))
    }
  }
}