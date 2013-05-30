package streamsend.pillar

import com.datastax.driver.core.Session

object Migration {
  def apply(description: String, authoredAt: Long, up: String): Migration = {
    new Migration(description, authoredAt, up, None)
  }

  def apply(description: String, authoredAt: Long, up: String, down: String): Migration = {
    new Migration(description,  authoredAt, up, Some(down))
  }
}

class Migration(val description: String, val authoredAt: Long, upStatement: String, downStatement: Option[String]) {
  def up(session: Session) {
    session.execute(upStatement)
  }
}
