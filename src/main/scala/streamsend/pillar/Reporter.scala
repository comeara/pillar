package streamsend.pillar

import java.util.Date

trait Reporter {
  def initializing(dataStore: DataStore, replicationOptions: ReplicationOptions)
  def migrating(dataStore: DataStore, dateRestriction: Option[Date])
  def applying(migration: Migration)
  def reversing(migration: Migration)
}