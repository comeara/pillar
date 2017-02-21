package com.chrisomeara.pillar.cli


trait MigratorAction

case object Migrate extends MigratorAction

case object Noop extends MigratorAction

case object Initialize extends MigratorAction
