package streamsend.pillar


object Migration {
  def apply(description: String, authoredAt: Long, up: String): Migration = {
    new IrreversibleMigration(description, authoredAt, up)
  }

  def apply(description: String, authoredAt: Long, up: String, down: String): Migration = {
    new ReversibleMigration(description, authoredAt, up, down)
  }
}

abstract class Migration {
  def description: String

  def authoredAt: Long

  def up: String
}

case class IrreversibleMigration(description: String, authoredAt: Long, up: String) extends Migration

case class ReversibleMigration(description: String, authoredAt: Long, up: String, down: String) extends Migration

