package streamsend.pillar

import java.util.Date

object MigrationRegistry {
  def apply(migrations: Seq[Migration]): MigrationRegistry = {
    new MigrationRegistry(migrations)
  }
}

class MigrationRegistry(migrations: Seq[Migration]) {
  private val migrationsByKey = migrations.foldLeft(Map.empty[MigrationKey, Migration]) {
    (memo, migration) => memo + (migration.key -> migration)
  }

  def authoredBefore(date: Date): Seq[Migration] = {
    migrations.filter(migration => migration.authoredBefore(date))
  }

  def apply(key: MigrationKey): Migration = {
    migrationsByKey(key)
  }

  def all: Seq[Migration] = migrations
}