package com.chrisomeara.pillar.cli

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import com.typesafe.config.ConfigFactory
import com.chrisomeara.pillar.{Reporter, DataStore, Registry}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

class CommandSpec extends FunSpec with ShouldMatchers with MockitoSugar {
  describe(".buildFromConfiguration") {
    implicit val registryConstructor: ((File, Reporter) => Registry) = ((_, _) => Registry(List.empty))
    implicit val reporter = mock[Reporter]

    val commandLineConfiguration = new CommandLineConfiguration(Initialize,
      "faker",
      "test",
      new File("src/test/resources/pillar/migrations"),
      None
    )
    val applicationConfiguration = ConfigFactory.load()

    it("sets the action") {
      Command.buildFromConfiguration(commandLineConfiguration, applicationConfiguration).action should be(Initialize)
    }

    it("sets the data store") {
      Command.buildFromConfiguration(commandLineConfiguration, applicationConfiguration).dataStore should equal(DataStore("faker", "pillar_test", "127.0.0.1"))
    }

    it("sets the time stamp option") {
      Command.buildFromConfiguration(commandLineConfiguration, applicationConfiguration).timeStampOption should be(None)
    }

    it("sets the registry") {
      val registry = mock[Registry]
      val constructor = mock[((File, Reporter) => Registry)]
      stub(constructor.apply(new File("src/test/resources/pillar/migrations/faker"), reporter)).toReturn(registry)
      Command.buildFromConfiguration(commandLineConfiguration, applicationConfiguration)(constructor, reporter).registry should equal(registry)
    }
  }
}