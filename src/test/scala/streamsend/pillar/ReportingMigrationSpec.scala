package streamsend.pillar

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import com.datastax.driver.core.Session
import java.util.Date

class ReportingMigrationSpec extends FunSpec with ShouldMatchers with MockitoSugar {
  val reporter = mock[Reporter]
  val wrapped = mock[Migration]
  val migration = new ReportingMigration(reporter, wrapped)
  val session = mock[Session]

  describe("#executeUpStatement") {
    migration.executeUpStatement(session)

    it("reports the applying action") {
      verify(reporter).applying(wrapped)
    }

    it("delegates to the wrapped migration") {
      verify(wrapped).executeUpStatement(session)
    }
  }

  describe("#executeDownStatement") {
    migration.executeDownStatement(session)

    it("reports the reversing action") {
      verify(reporter).reversing(wrapped)
    }

    it("delegates to the wrapped migration") {
      verify(wrapped).executeDownStatement(session)
    }
  }
}
