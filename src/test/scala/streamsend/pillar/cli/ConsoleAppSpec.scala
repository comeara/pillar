package streamsend.pillar.cli

import org.scalatest.{FunSpec, BeforeAndAfter}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import scala.Array
import org.clapper.argot.ArgotUsageException
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.typesafe.config.Config
import streamsend.pillar.PrintStreamReporter

class ConsoleAppSpec extends FunSpec with BeforeAndAfter with ShouldMatchers with MockitoSugar {

  describe("#run") {
    describe("empty arguments") {
      val app = ConsoleApp()
      val arguments = Array.empty[String]

      it("raises a usage exception") {
        intercept[ArgotUsageException] {
          app.run(arguments)
        }
      }
    }

    describe("initialize faker") {
      val executor = mock[PillarCommandExecutor]
      val executorConstructor = mock[(() => PillarCommandExecutor)]
      stub(executorConstructor.apply()).toReturn(executor)
      val command  = mock[PillarCommand]
      val commandConstructor = mock[((CommandLineConfiguration, Config) => PillarCommand)]
      stub(commandConstructor.apply(any[CommandLineConfiguration], any[Config])).toReturn(command)

      new ConsoleApp()(commandConstructor, executorConstructor).run(Array("initialize", "faker"))

      it("executes the command") {
        verify(executor).execute(org.mockito.Matchers.eq(command), any[PrintStreamReporter])
      }
    }
  }
}
