package streamsend.pillar.cli

import java.util.Date
import streamsend.pillar.{Reporter, DataStore, MigrationRegistry, Migrator}

object PillarCommandExecutor {
  implicit private val migratorConstructor: ((DataStore, MigrationRegistry, Reporter) => Migrator) = Migrator.apply

  def apply(): PillarCommandExecutor = new PillarCommandExecutor()
}

class PillarCommandExecutor(implicit val migratorConstructor: ((DataStore, MigrationRegistry, Reporter) => Migrator)) {
  def execute(command: PillarCommand, reporter: Reporter) {
    val migrator = migratorConstructor(command.dataStore, command.registry, reporter)

    command.action match {
      case Initialize => migrator.initialize(command.dataStore)
      case Migrate    => migrator.migrate(command.dataStore, command.timeStampOption.map(new Date(_)))
    }
  }
}