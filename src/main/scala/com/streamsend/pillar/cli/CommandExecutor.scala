package com.streamsend.pillar.cli

import java.util.Date
import com.streamsend.pillar.{Reporter, DataStore, Registry, Migrator}

object CommandExecutor {
  implicit private val migratorConstructor: ((Registry, Reporter) => Migrator) = Migrator.apply

  def apply(): CommandExecutor = new CommandExecutor()
}

class CommandExecutor(implicit val migratorConstructor: ((Registry, Reporter) => Migrator)) {
  def execute(command: Command, reporter: Reporter) {
    val migrator = migratorConstructor(command.registry, reporter)

    command.action match {
      case Initialize => migrator.initialize(command.dataStore)
      case Migrate    => migrator.migrate(command.dataStore, command.timeStampOption.map(new Date(_)))
    }
  }
}