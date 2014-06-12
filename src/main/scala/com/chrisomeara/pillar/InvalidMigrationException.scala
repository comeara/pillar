package com.chrisomeara.pillar

class InvalidMigrationException(val errors: Map[String,String]) extends RuntimeException
