package streamsend.pillar

object ReplicationOptions {
  val default = Map("class" -> "SimpleStrategy", "replication_factor" -> 3)
}
