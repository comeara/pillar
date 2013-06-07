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
1. Cassandra 1.2 with the native CQL protocol enabled

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

Coming soon.

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

  1. Write migrations, place them in conf/pillar/myapp/migrations.
  1. Add pillar settings to conf/application.conf.
  1. % pillar initialize myapp
  1. % pillar migrate myapp

#### Migration Files

Migration files contain metadata about the migration, a [CQL][cql] statement used to apply the migration and,
optionally, a [CQL][cql] statement used to reverse the migration. Each file describes one migration. You probably
want to name your files according to time stamp and description, 1370028263_creates_views_table.cql, for example.
Pillar reads and parses all files in the migrations directory, regardless of file name.

[cql]:http://cassandra.apache.org/doc/cql3/CQL.html

Pillar supports reversible, irreversible and reversible with a no-op down statement migrations. Here are examples of
each:

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
        }
    }

[typesafeconfig]:https://github.com/typesafehub/config

Reference the [acceptance spec suite][cliacceptance] for details.

[cliacceptance]:https://github.com/comeara/pillar/blob/master/src/test/scala/streamsend/pillar/PillarCommandLineAcceptanceSpec.scala

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
Reference the [acceptance spec suite][libacceptance] for details.

[libacceptance]:https://github.com/comeara/pillar/blob/master/src/test/scala/streamsend/pillar/PillarLibraryAcceptanceSpec.scala

