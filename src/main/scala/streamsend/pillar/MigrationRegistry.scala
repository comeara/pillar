package streamsend.pillar

import java.util.Date
import java.io.{FileInputStream, File}

object MigrationRegistry {
  def apply(migrations: Seq[Migration]): MigrationRegistry = {
    new MigrationRegistry(migrations)
  }

  def fromDirectory(directory: File): MigrationRegistry = {
    if(!directory.isDirectory) return new MigrationRegistry(List.empty)

    val parser = MigrationParser()

    new MigrationRegistry(directory.listFiles().map {
      file =>
        val stream = new FileInputStream(file)
        try {
          parser.parse(stream)
        } finally {
          stream.close()
        }
    }.toList)
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