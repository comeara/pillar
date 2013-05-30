package streamsend.pillar

import java.util.Date


object Migration {
  def apply(description: String, authoredAt: Date, up: String): Migration = {
    new IrreversibleMigration(description, authoredAt, up)
  }

  def apply(description: String, authoredAt: Date, up: String, down: Option[String]): Migration = {
    down match {
      case Some(downStatment) =>
        new ReversibleMigration(description, authoredAt, up, downStatment)
      case None =>
        new ReversibleMigrationWithNoopDown(description, authoredAt, up)
    }
  }
}

abstract class Migration {
  def description: String

  def authoredAt: Date

  def up: String
}

case class IrreversibleMigration(description: String, authoredAt: Date, up: String) extends Migration

case class ReversibleMigrationWithNoopDown(description: String, authoredAt: Date, up: String) extends Migration

case class ReversibleMigration(description: String, authoredAt: Date, up: String, down: String) extends Migration

