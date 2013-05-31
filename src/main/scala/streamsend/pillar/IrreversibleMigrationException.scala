package streamsend.pillar

class IrreversibleMigrationException(migration: IrreversibleMigration)
  extends RuntimeException("Migration %d (%s) is not reversible".format(migration.authoredAt.getTime, migration.description))