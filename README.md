# Pillar

Pillar manages migrations for your [Cassandra][cassandra] data stores.

[cassandra]:http://cassandra.apache.org

Pillar grew from a desire to automatically manage Cassandra schema as code. Managing schema as code enables automated
build and deployment, a foundational practice for an organization striving to achieve [Continuous Delivery][cd].

Pillar is to Cassandra what [Rails ActiveRecord][ar] migrations or [Play Evolutions][evolutions] are to relational
databases with one key difference: Pillar is completely independent from any application development framework.

[cd]:http://en.wikipedia.org/wiki/Continuous_delivery
[ar]:https://github.com/rails/rails/tree/master/activerecord
[evolutions]:http://www.playframework.com/documentation/2.0/Evolutions

## Installation

### Prerequisites

1. Java SE 6 runtime environment
1. Cassandra 2.0 or 2.1 with the native CQL protocol enabled

### From Source

This method requires [Simple Build Tool (sbt)][sbt].
Building an RPM also requires [Effing Package Management (fpm)][fpm].

    % sbt assembly   # builds just the jar file in the target/ directory

    % sbt rh-package # builds the jar and the RPM in the target/ directory
    % sudo rpm -i target/pillar-1.0.0-DEV.noarch.rpm

The RPM installs Pillar to /opt/pillar.

[sbt]:http://www.scala-sbt.org
[fpm]:https://github.com/jordansissel/fpm

### Packages

Pillar is available at Maven Central under the GroupId com.chrisomeara and ArtifactId pillar_2.10 or pillar_2.11. The current version is 2.1.1.

#### sbt

  libraryDependencies += "com.chrisomeara" % "pillar_2.10" % "2.1.1"

#### Gradle

  compile 'com.chrisomeara:pillar_2.10:2.1.1'

## Usage

### Terminology

Data Store
: A logical grouping of environments. You will likely have one data store per application.

Environment
: A context or grouping of settings for a single data store. You will likely have at least development and production
environments for each data store.

Migration
: A single change to a data store. Migrations have a description and a time stamp indicating the time at which it was
authored. Migrations are applied in ascending order and reversed in descending order.

### Command Line

Here's the short version:

  1. Write migrations, place them in conf/pillar/migrations/myapp.
  1. Add pillar settings to conf/application.conf.
  1. % pillar initialize myapp
  1. % pillar migrate myapp

#### Migration Files

Migration files contain metadata about the migration, a [CQL][cql] statement used to apply the migration and,
optionally, a [CQL][cql] statement used to reverse the migration. Each file describes one migration. You probably
want to name your files according to time stamp and description, 1370028263000_creates_views_table.cql, for example.
Pillar reads and parses all files in the migrations directory, regardless of file name.

[cql]:http://cassandra.apache.org/doc/cql3/CQL.html

Pillar supports reversible, irreversible and reversible with a no-op down statement migrations. Here are examples of
each:

Reversible migrations have up and down properties.

    -- description: creates views table
    -- authoredAt: 1370028263000
    -- up:

    CREATE TABLE views (
      id uuid PRIMARY KEY,
      url text,
      person_id int,
      viewed_at timestamp
    )

    -- down:

    DROP TABLE views

Irreversible migrations have an up property but no down property.

    -- description: creates events table
    -- authoredAt: 1370023262000
    -- up:

    CREATE TABLE events (
      batch_id text,
      occurred_at uuid,
      event_type text,
      payload blob,
      PRIMARY KEY (batch_id, occurred_at, event_type)
    )

Reversible migrations with no-op down statements have an up property and an empty down property.

    -- description: adds user_agent to views table
    -- authoredAt: 1370028264000
    -- up:

    ALTER TABLE views
    ADD user_agent text

    -- down:

The Pillar command line interface expects to find migrations in conf/pillar/migrations unless overriden by the
-d command-line option.

#### Configuration

Pillar uses the [Typesafe Config][typesafeconfig] library for configuration. The Pillar command-line interface expects
to find an application.conf file in ./conf or ./src/main/resources. Given a data store called faker, the
application.conf might look like the following:

    pillar.faker {
        development {
            cassandra-seed-address: "127.0.0.1"
            cassandra-keyspace-name: "pillar_development"
        }
        test {
            cassandra-seed-address: "127.0.0.1"
            cassandra-keyspace-name: "pillar_test"
        }
        acceptance_test {
            cassandra-seed-address: "127.0.0.1"
            cassandra-keyspace-name: "pillar_acceptance_test"
            cassandra-port: "9042"
            cassandra-ssl: "true"
        }
    }

[typesafeconfig]:https://github.com/typesafehub/config

Alternatively, Pillar accepts environment variable overrides according to the following table.

| Key                                            | Environment Variable | Default |
|------------------------------------------------|----------------------|---------|
| pillar.\<store>.\<env>.cassandra-seed-address  | PILLAR_SEED_ADDRESS  |         |
| pillar.\<store>.\<env>.cassandra-keyspace-name |                      |         |
| pillar.\<store>.\<env>.cassandra-port          | PILLAR_PORT          | 9042    |
| pillar.\<store>.\<env>.cassandra-ssl           | PILLAR_SSL           | false   |

Reference the acceptance spec suite for details.

#### The pillar Executable

The package installs to /opt/pillar by default. The /opt/pillar/bin/pillar executable usage looks like this:

    Usage: pillar [OPTIONS] command data-store

    OPTIONS

    -d directory
    --migrations-directory directory  The directory containing migrations

    -e env
    --environment env                 environment

    -t time
    --time-stamp time                 The migration time stamp

    PARAMETERS

    command     migrate or initialize

    data-store  The target data store, as defined in application.conf

#### Examples

Initialize the faker datastore development environment

    % pillar -e development initialize faker

Apply all migrations to the faker datastore development environment

    % pillar -e development migrate faker

### Library

You can also integrate Pillar directly into your application as a library.
Reference the acceptance spec suite for details.

### Forks

Several organizations and people have forked the Pillar code base. The most actively maintained alternative is
the [Galeria-Kaufhof fork][gkf].

[gkf]:https://github.com/Galeria-Kaufhof/pillar

### Release Notes

#### 1.0.1

* Add a "destroy" method to drop a keyspace (iamsteveholmes)

#### 1.0.3

* Clarify documentation (pvenable)
* Update Datastax Cassandra driver to version 2.0.2 (magro)
* Update Scala to version 2.10.4 (magro)
* Add cross-compilation to Scala version 2.11.1 (magro)
* Shutdown cluster in migrate & initialize (magro)
* Transition support from StreamSend to Chris O'Meara (comeara)

#### 2.0.0

* Allow configuration of Cassandra port (fkoehler)
* Rework Migrator interface to allow passing a Session object when integrating Pillar as a library (magro, comeara)

#### 2.0.1

* Update a argot dependency to version 1.0.3 (magro)

#### 2.1.0

* Update Datastax Cassandra driver to version 3.0.0 (MarcoPriebe)
* Fix documentation issue where authored_at represented as seconds rather than milliseconds (jhungerford)
* Introduce PILLAR_SEED_ADDRESS environment variable (comeara)

#### 2.1.1

* Fix deduplicate error during merge, ref. issue #32 (ilovezfs)

#### 2.2.0

* Add feature to read registry from files (sadowskik)
