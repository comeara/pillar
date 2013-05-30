package streamsend.pillar

import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import scala.collection.JavaConversions

object AppliedMigrations {
  def apply(session: Session): Seq[MigrationKey] = {
    val results = session.execute(QueryBuilder.select("authored_at", "description").from("applied_migrations"))
    JavaConversions.asScalaBuffer(results.all()).map {
      row => MigrationKey(row.getDate("authored_at"), row.getString("description"))
    }
  }
}