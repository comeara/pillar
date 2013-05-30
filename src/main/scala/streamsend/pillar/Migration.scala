package streamsend.pillar

import com.datastax.driver.core.Session

object Migration {
  def apply(id: String, up: String): Migration = {
    new Migration(id, up, None)
  }

  def apply(id: String, up: String, down: String): Migration = {
    new Migration(id, up, Some(down))
  }
}

class Migration(val id: String, upStatement: String, downStatement: Option[String]) {
  def up(session: Session) {
    session.execute(upStatement)
  }
}
