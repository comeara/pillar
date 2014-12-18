package com.chrisomeara.pillar

import java.util.Date
import java.io.{FileInputStream, File}

object Registry {

  def apply(migrations: Seq[Migration]): Registry = {
    new Registry(migrations)
  }

  def fromDirectory(directory: File, reporter: Reporter): Registry = {
    new Registry(parseMigrationsInDirectory(directory).map(new ReportingMigration(reporter, _)))
  }

  def fromDirectory(directory: File): Registry = {
    new Registry(parseMigrationsInDirectory(directory))
  }

  def fromFiles(files: Seq[File]): Registry = {
    new Registry(parseMigrationsInFiles(filterExisting(files)))
  }

  def fromFiles(files: Seq[File], reporter: Reporter): Registry = {
    new Registry(
      parseMigrationsInFiles(filterExisting(files))
        .map(new ReportingMigration(reporter, _))
    )
  }

  private def filterExisting(files : Seq[File]) : Seq[File] = {
    files
      .filterNot(file => file.isDirectory)
      .filter(file => file.exists())
  }

  private def parseMigrationsInFiles(files: Seq[File]): Seq[Migration] = {
    val parser = Parser()

    files.map {
      file =>
        val stream = new FileInputStream(file)
        try {
          parser.parse(stream)
        } finally {
          stream.close()
        }
    }.toList
  }

  private def parseMigrationsInDirectory(directory: File): Seq[Migration] = {
    if (!directory.isDirectory)
      return List.empty

    parseMigrationsInFiles(directory.listFiles())
  }
}

class Registry(private var migrations: Seq[Migration]) {
  migrations = migrations.sortBy(_.authoredAt)

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