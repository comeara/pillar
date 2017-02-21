# Pillar

[![Maven Central](https://img.shields.io/maven-central/v/com.chrisomeara/pillar_2.12.svg)][pillar_2.12]

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

1. Java SE 6 or more recent runtime environment
1. Cassandra 2.x or 3.x

### From Source

This method requires [Simple Build Tool (sbt)][sbt].

```
% sbt assembly   # builds a fat jar file in the target/ directory
```

[sbt]:http://www.scala-sbt.org

### Packages

Pillar is available at Maven Central under the GroupId com.chrisomeara and ArtifactId [pillar_2.10][pillar_2.10],
[pillar_2.11][pillar_2.11] or [pillar_2.12][pillar_2.12]. The current version is 3.0.0.

#### sbt

```
libraryDependencies += "com.chrisomeara" %% "pillar" % "3.0.0"
```

#### Gradle

```
compile 'com.chrisomeara:pillar_2.12:3.0.0'
```

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
  1. % pillar initialize -e development myapp
  1. % pillar migrate -e development myapp

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

Each migration may optionally specify multiple stages. Stages are executed in the order specified.

    -- description: creates users and groups tables
    -- authoredAt: 1469630066000
    -- up:

    -- stage: 1
    CREATE TABLE groups (
      id uuid,
      name text,
      PRIMARY KEY (id)
    )

    -- stage: 2
    CREATE TABLE users (
      id uuid,
      group_id uuid,
      username text,
      password text,
      PRIMARY KEY (id)
    )


    -- down:

    -- stage: 1
    DROP TABLE users

    -- stage: 2
    DROP TABLE groups


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
            cassandra-seed-address: ${?PILLAR_SEED_ADDRESS}
            cassandra-port: ${?PILLAR_PORT}
            cassandra-keyspace-name: ${?PILLAR_KEYSPACE_NAME}
            cassandra-ssl: ${?PILLAR_SSL}
            cassandra-username: ${?PILLAR_USERNAME}
            cassandra-password: ${?PILLAR_PASSWORD}
        }
    }

[typesafeconfig]:https://github.com/typesafehub/config

Notice the use of environment varaibles in the acceptance_test environment example. This is a feature of Typesafe Config
that can greatly increase the security and portability of your Pillar configuration.

#### Transport Layer Security (TLS/SSL)

Pillar will optionally enable TLS/SSL for client-to-node communications. As Pillar runs on the Java virtual machine,
normal JVM TLS/SSL configuration options apply. If the JVM executing Pillar does not already trust the certificate
presented by the Cassandra cluster, you may need to configure the trust store as documented by [Oracle][jsseref]
and [DataStax][dsssl].

Pillar does not install a custom trust manager but rather relies on the default trust manager implementation.
Configuring the default trust store requires setting two system properties, like this:

    JAVA_OPTS='-Djavax.net.ssl.trustStore=/opt/pillar/conf/truststore -Djavax.net.ssl.trustStorePassword=cassandra'

$JAVA_OPTS are passed through to the JVM when using the pillar executable.

[jsseref]:https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html
[dsssl]:https://datastax.github.io/java-driver/2.0.12/features/ssl/

#### The pillar Executable

The Pillar executable usage looks like this:

    Usage: pillar [initialize|migrate] data-store

    Command: initialize [options]

      -e, --environment <value>

    Command: migrate [options]

      -e, --environment <value>

      -t, --time-stamp <value>

      -d, --migrations-directory <value>

      data-store

#### Examples

Initialize the faker datastore development environment

    % pillar initialize -e development faker

Apply all migrations to the faker datastore development environment

    % pillar migrate -e development faker

### Library

You can also integrate Pillar directly into your application as a library. Reference the [pillar-core][core] repository for
more information regarding Pillar library integration.

[core]:https://github.com/comeara/pillar-core

## Forks

Several organizations and people have forked the Pillar code base. The most actively maintained alternative is
the [Galeria-Kaufhof fork][gkf].

[gkf]:https://github.com/Galeria-Kaufhof/pillar

## Change Log

Please reference the [Pillar Changes][changes] document.

[changes]: CHANGES.md

## Upgrade Instructions

Please reference the [Pillar Upgrades][upgrade] document.

[upgrade]: UPGRADE.md



[pillar_2.10]: https://maven-badges.herokuapp.com/maven-central/com.chrisomeara/pillar_2.10
[pillar_2.11]: https://maven-badges.herokuapp.com/maven-central/com.chrisomeara/pillar_2.11
[pillar_2.12]: https://maven-badges.herokuapp.com/maven-central/com.chrisomeara/pillar_2.12
