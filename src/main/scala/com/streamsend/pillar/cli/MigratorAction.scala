package com.streamsend.pillar.cli


trait MigratorAction

case object Migrate extends MigratorAction

case object Initialize extends MigratorAction
