package streamsend.pillar

import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import scala.collection.JavaConversions
import java.util.Date

object AppliedMigrations {
  def apply(session: Session, registry: MigrationRegistry): AppliedMigrations = {
    val results = session.execute(QueryBuilder.select("authored_at", "description").from("applied_migrations"))
    new AppliedMigrations(JavaConversions.asScalaBuffer(results.all()).map {
      row => registry(MigrationKey(row.getDate("authored_at"), row.getString("description")))
    })
  }
}

class AppliedMigrations(applied: Seq[Migration]) extends Seq[Migration] {
  def length: Int = applied.length

  def apply(index: Int): Migration = applied.apply(index)

  def iterator: Iterator[Migration] = applied.iterator

  def authoredAfter(date: Date): Seq[Migration] = applied.filter(migration => migration.authoredAfter(date))
}