package streamsend.pillar.cli

import com.typesafe.config.{Config, ConfigFactory}
import streamsend.pillar.{PrintStreamReporter, MigrationRegistry}
import java.io.File

object App {
  implicit private val commandConstructor: ((CommandLineConfiguration, Config) => PillarCommand) = PillarCommand.buildFromConfiguration
  implicit private val executorConstructor: (() => PillarCommandExecutor) = PillarCommandExecutor.apply
  implicit private val registryConstructor: ((File) => MigrationRegistry) = MigrationRegistry.fromDirectory

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

class App(implicit commandConstructor: ((CommandLineConfiguration, Config) => PillarCommand), executorConstructor: (() => PillarCommandExecutor)) {
  def run(arguments: Array[String]) {
    val commandLineConfiguration = CommandLineConfiguration.buildFromArguments(arguments)
    val command = commandConstructor(commandLineConfiguration, ConfigFactory.load())
    val reporter = new PrintStreamReporter(System.out)
    executorConstructor().execute(command, reporter)
  }
}