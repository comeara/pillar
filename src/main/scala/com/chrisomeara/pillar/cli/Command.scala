package com.chrisomeara.pillar.cli

import com.chrisomeara.pillar.Registry
import com.datastax.driver.core.Session

case class Command(action: MigratorAction, session: Session, keyspace: String, timeStampOption: Option[Long], registry: Registry)