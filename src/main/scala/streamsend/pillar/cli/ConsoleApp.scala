package streamsend.pillar.cli

import com.typesafe.config.{Config, ConfigFactory}
import streamsend.pillar.MigrationRegistry
import java.io.File

object ConsoleApp {
  implicit private val commandConstructor: ((CommandLineConfiguration, Config) => PillarCommand) = PillarCommand.buildFromConfiguration
  implicit private val executorConstructor: (() => PillarCommandExecutor) = PillarCommandExecutor.apply
  implicit private val registryConstructor: ((File) => MigrationRegistry) = MigrationRegistry.fromDirectory

  def apply(): ConsoleApp = {
    new ConsoleApp()
  }

  def main(arguments: Array[String]) {
    try {
      ConsoleApp().run(arguments)
    } catch {
      case exception: Exception =>
        System.err.println(exception.getMessage)
        System.exit(1)
    }

    System.exit(0)
  }
}

class ConsoleApp(implicit
                 commandConstructor: ((CommandLineConfiguration, Config) => PillarCommand),
                 executorConstructor: (() => PillarCommandExecutor)) {
  def run(arguments: Array[String]) {
    val commandLineConfiguration = CommandLineConfiguration.buildFromArguments(arguments)
    val command = commandConstructor(commandLineConfiguration, ConfigFactory.load())
    executorConstructor().execute(command)
  }
}