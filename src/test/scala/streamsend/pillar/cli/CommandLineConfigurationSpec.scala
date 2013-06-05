package streamsend.pillar.cli

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class CommandLineConfigurationSpec extends FunSpec with ShouldMatchers {
  describe(".buildFromArguments") {
    describe("command initialize") {
      describe("data-store faker") {
        it("sets the command") {
          CommandLineConfiguration.buildFromArguments(Array("initialize", "faker")).command should be(Initialize)
        }

        it("sets the data store name") {
          CommandLineConfiguration.buildFromArguments(Array("initialize", "faker")).dataStore should equal("faker")
        }

        it("sets the environment") {
          CommandLineConfiguration.buildFromArguments(Array("initialize", "faker")).environment should equal("development")
        }

        it("sets the migrations directory") {
          CommandLineConfiguration.buildFromArguments(Array("initialize", "faker")).migrationsDirectory.getPath should equal("config/pillar/migrations")
        }

        it("sets the time stamp") {
          CommandLineConfiguration.buildFromArguments(Array("initialize", "faker")).timeStampOption should be(None)
        }

        describe("environment test") {
          it("sets the environment") {
            CommandLineConfiguration.buildFromArguments(Array("-e", "test", "initialize", "faker")).environment should equal("test")
          }
        }

        describe("migrations-directory baz") {
          it("sets the migrations directory") {
            CommandLineConfiguration.buildFromArguments(Array("-d", "src/test/resources/pillar/migrations", "initialize", "faker")).migrationsDirectory.getPath should equal("src/test/resources/pillar/migrations")
          }
        }

        describe("time-stamp 1370028262") {
          it("sets the time stamp option") {
            CommandLineConfiguration.buildFromArguments(Array("-t", "1370028262", "initialize", "faker")).timeStampOption should equal(Some(1370028262))
          }
        }
      }
    }
  }
}