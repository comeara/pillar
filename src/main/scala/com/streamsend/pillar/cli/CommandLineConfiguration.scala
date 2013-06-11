package com.streamsend.pillar.cli

import org.clapper.argot.ArgotParser
import org.clapper.argot.ArgotConverters._
import java.io.File

object CommandLineConfiguration {
  def buildFromArguments(arguments: Array[String]): CommandLineConfiguration = {
    val parser = new ArgotParser("pillar")

    val commandParameter = parser.parameter[MigratorAction]("command", "migrate or initialize", optional = false) {
      (commandString, _) =>
        commandString match {
          case "initialize" => Initialize
          case "migrate" => Migrate
          case _ => parser.usage(s"$commandString is not a command")
        }
    }
    val dataStoreConfigurationOption = parser.parameter[String]("data-store", "The target data store, as defined in application.conf", optional = false)
    val migrationsDirectoryOption = parser.option[File](List("d", "migrations-directory"), "directory", "The directory containing migrations") {
      (path, _) =>
        val directory = new File(path)
        if (!directory.isDirectory) parser.usage(s"${directory.getAbsolutePath} is not a directory")
        directory
    }
    val environmentOption = parser.option[String](List("e", "environment"), "env", "environment")
    val timeStampOption = parser.option[Long](List("t", "time-stamp"), "time", "The migration time stamp")

    parser.parse(arguments)

    CommandLineConfiguration(
      commandParameter.value.get,
      dataStoreConfigurationOption.value.get,
      environmentOption.value.getOrElse("development"),
      migrationsDirectoryOption.value.getOrElse(new File("conf/pillar/migrations")),
      timeStampOption.value
    )
  }
}

case class CommandLineConfiguration(command: MigratorAction, dataStore: String, environment: String, migrationsDirectory: File, timeStampOption: Option[Long])

