package streamsend.pillar

import java.io.PrintStream
import java.util.Date

class PrintStreamReporter(stream: PrintStream) extends streamsend.pillar.Reporter {
  def initializing(dataStore: DataStore, replicationOptions: ReplicationOptions) {
    stream.println(s"Initializing ${dataStore.name} data store")
  }

  def migrating(dataStore: DataStore, dateRestriction: Option[Date]) {
    stream.println(s"Migrating ${dataStore.name} data store")
  }

  def applying(migration: Migration) {}

  def reversing(migration: Migration) {}
}