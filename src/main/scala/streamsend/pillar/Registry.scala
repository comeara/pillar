package streamsend.pillar

import java.util.Date
import java.io.{FileInputStream, File}

object Registry {
  def apply(migrations: Seq[Migration]): Registry = {
    new Registry(migrations)
  }

  def fromDirectory(directory: File): Registry = {
    if(!directory.isDirectory) return new Registry(List.empty)

    val parser = Parser()

    new Registry(directory.listFiles().map {
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

class Registry(migrations: Seq[Migration]) {
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