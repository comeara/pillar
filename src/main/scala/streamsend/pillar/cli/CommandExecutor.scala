package streamsend.pillar.cli

import java.util.Date
import streamsend.pillar.{Reporter, DataStore, Registry, Migrator}

object CommandExecutor {
  implicit private val migratorConstructor: ((DataStore, Registry, Reporter) => Migrator) = Migrator.apply

  def apply(): CommandExecutor = new CommandExecutor()
}

class CommandExecutor(implicit val migratorConstructor: ((DataStore, Registry, Reporter) => Migrator)) {
  def execute(command: Command, reporter: Reporter) {
    val migrator = migratorConstructor(command.dataStore, command.registry, reporter)

    command.action match {
      case Initialize => migrator.initialize(command.dataStore)
      case Migrate    => migrator.migrate(command.dataStore, command.timeStampOption.map(new Date(_)))
    }
  }
}