package com.chrisomeara.pillar.cli

import scopt.OptionParser
import java.io.File

object CommandLineConfiguration {
  def buildFromArguments(arguments: Array[String]): CommandLineConfiguration = {
    val parser = new OptionParser[CommandLineConfiguration]("pillar") {

      cmd("initialize").action((_, c) => c.copy(command = Initialize)).children(
        opt[String]('e', "environment").optional().action((e, c) => c.copy(environment = e))
      )

      cmd("migrate").action((_, c) => c.copy(command = Migrate)).children(
        opt[String]('e', "environment").optional().action((e, c) => c.copy(environment = e)),
        opt[Long]('t', "time-stamp").optional().action((t, c) => c.copy(timeStampOption = Some(t))),
        opt[File]('d', "migrations-directory").optional().action((d, c) => c.copy(migrationsDirectory = d))
      )

      arg[String]("data-store").action((ds, c) => c.copy(dataStore = ds))
    }

    parser.parse(arguments, CommandLineConfiguration()) match {
      case Some(configuration) =>
        configuration
      case None =>
        throw new IllegalArgumentException("Unable to construct configuration from command line arguments")
    }
  }
}

case class CommandLineConfiguration(command: MigratorAction = Noop, dataStore: String = "", environment: String = "development", migrationsDirectory: File = new File("conf/pillar/migrations"), timeStampOption: Option[Long] = None)
