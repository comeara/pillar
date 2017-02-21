# Pillar Changes

## 1.0.1

* Add a "destroy" method to drop a keyspace (iamsteveholmes)

## 1.0.3

* Clarify documentation (pvenable)
* Update DataStax Cassandra driver to version 2.0.2 (magro)
* Update Scala to version 2.10.4 (magro)
* Add cross-compilation to Scala version 2.11.1 (magro)
* Shutdown cluster in migrate & initialize (magro)
* Transition support from StreamSend to Chris O'Meara (comeara)

## 2.0.0

* Allow configuration of Cassandra port (fkoehler)
* Rework Migrator interface to allow passing a Session object when integrating Pillar as a library (magro, comeara)

## 2.0.1

* Update a argot dependency to version 1.0.3 (magro)

## 2.1.0

* Update DataStax Cassandra driver to version 3.0.0 (MarcoPriebe)
* Fix documentation issue where authored_at represented as seconds rather than milliseconds (jhungerford)
* Introduce PILLAR_SEED_ADDRESS environment variable (comeara)

## 2.1.1

* Fix deduplicate error during merge, ref. issue #32 (ilovezfs)

## 2.2.0

* Add feature to read registry from files (sadowskik)
* Add TLS/SSL support(bradhandy, comeara)
* Add authentication support (bradhandy, comeara)

## 2.3.0

* Add multiple stages per migration (sadowskik)

## 3.0.0

* Support Scala 2.12 (comeara)
* Split Pillar command line interface and core library into separate artifacts (comeara)
* Add SLF4J binding for command line interface (comeara)
* Update command-line interface to use command and sub-command structure (comeara)
* Remove RPM build (comeara)