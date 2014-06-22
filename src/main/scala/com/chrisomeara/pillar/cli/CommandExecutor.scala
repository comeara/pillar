package com.chrisomeara.pillar.cli

import java.util.Date

import com.chrisomeara.pillar.{Migrator, Registry, Reporter}

object CommandExecutor {
  implicit private val migratorConstructor: ((Registry, Reporter) => Migrator) = Migrator.apply

  def apply(): CommandExecutor = new CommandExecutor()
}

class CommandExecutor(implicit val migratorConstructor: ((Registry, Reporter) => Migrator)) {
  def execute(command: Command, reporter: Reporter) {
    val migrator = migratorConstructor(command.registry, reporter)

    command.action match {
      case Initialize => migrator.initialize(command.session, command.keyspace)
      case Migrate => migrator.migrate(command.session, command.timeStampOption.map(new Date(_)))
    }
  }
}