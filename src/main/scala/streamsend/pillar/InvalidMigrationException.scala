package streamsend.pillar

class InvalidMigrationException(val errors: Map[String,String]) extends RuntimeException
