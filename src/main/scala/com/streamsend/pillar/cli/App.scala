package com.streamsend.pillar.cli

import com.typesafe.config.{Config, ConfigFactory}
import com.streamsend.pillar.{Reporter, PrintStreamReporter, Registry}
import java.io.File

object App {
  implicit private val reporter = new PrintStreamReporter(System.out)
  implicit private val commandConstructor: ((CommandLineConfiguration, Config) => Command) = Command.buildFromConfiguration
  implicit private val executorConstructor: (() => CommandExecutor) = CommandExecutor.apply
  implicit private val registryConstructor: ((File, Reporter) => Registry) = Registry.fromDirectory

  def apply(): App = {
    new App()
  }

  def main(arguments: Array[String]) {
    try {
      App().run(arguments)
    } catch {
      case exception: Exception =>
        System.err.println(exception.getMessage)
        System.exit(1)
    }

    System.exit(0)
  }
}

class App(implicit commandConstructor: ((CommandLineConfiguration, Config) => Command), executorConstructor: (() => CommandExecutor)) {
  def run(arguments: Array[String]) {
    val commandLineConfiguration = CommandLineConfiguration.buildFromArguments(arguments)
    val command = commandConstructor(commandLineConfiguration, ConfigFactory.load())
    executorConstructor().execute(command, App.reporter)
  }
}