package com.chrisomeara.pillar

import java.util.Date
import com.datastax.driver.core.Session

class ReportingMigration(reporter: Reporter, wrapped: Migration) extends Migration {
  val description: String = wrapped.description
  val authoredAt: Date = wrapped.authoredAt
  val up: Seq[String] = wrapped.up

  override def executeUpStatement(session: Session) {
    reporter.applying(wrapped)
    wrapped.executeUpStatement(session)
  }

  def executeDownStatement(session: Session) {
    reporter.reversing(wrapped)
    wrapped.executeDownStatement(session)
  }
}