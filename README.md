# Pillar

Pillar manages migrations for your [Cassandra][cassandra] data stores.

[cassandra]:http://cassandra.apache.org

## Installation

### From A Distribution

I'm still working on a binary distribution.

### From Source

This method requires [Simple Build Tool (sbt)][sbt]. Building an RPM also requires [Effing Package Management (fpm)][fpm].

    % sbt assembly # builds just the jar file to the target/ directory

    % sbt rh-package # builds an RPM to the target/ directory

The RPM installs Pillar to /opt/pillar

[sbt]:http://www.scala-sbt.org
[fpm]:https://github.com/jordansissel/fpm

## Usage

### Command Line

#### Terminology

Data Store
: A logical grouping of environments. You will likely have one data store per application.
Environment
: A context or grouping of settings for a single data store. You will likely have at least development and production environments for each data store.
Migration
: A single change to a data store. Migrations have a description and a time stamp indicating the time at which it was authored. Migrations are applied
in ascending order and reversed in descending order.

#### Migration Files

Migration files contain metadata about the migration, a [CQL][cql] statement used to apply the migration and, optionally, a CQL[cql] statement used to reverse the migration.

[cql]:http://cassandra.apache.org/doc/cql3/CQL.html

Pillar supports reversible, irreversible and reversible with a no-op down statement migrations. Here are examples of each:

Reversible migrations have up and down properties.

    -- description: creates views table
    -- authoredAt: 1370028263
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
    -- authoredAt: 1370023262
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
    -- authoredAt: 1370028264
    -- up:

    ALTER TABLE views
    ADD user_agent text

    -- down:

The Pillar command line interface expects to find migrations in conf/pillar/migrations unless overriden by the -d command-line option.

#### Configuration

Pillar uses the [Typesafe Config][typesafeconfig] library for configuration. The Pillar command-line interface expects to find an application.conf file in
./conf or ./src/main/resources. Given an data store called faker, the application.conf might look like the following:

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
        }
    }

[typesafeconfig]:https://github.com/typesafehub/config

Reference the [acceptance spec suite][cliacceptance] for details.

[cliacceptance]:https://github.com/comeara/pillar/blob/master/src/test/scala/streamsend/pillar/PillarCommandLineAcceptanceSpec.scala

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

### Library

You can also integrate Pillar directly into your application as a library.
Reference the [acceptance spec suite][libacceptance] for details.

[libacceptance]:https://github.com/comeara/pillar/blob/master/src/test/scala/streamsend/pillar/PillarLibraryAcceptanceSpec.scala

