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

### Library

Reference the [acceptance spec suite][libacceptance] for details.

[libacceptance]:https://github.com/comeara/pillar/blob/master/src/test/scala/streamsend/pillar/PillarLibraryAcceptanceSpec.scala

### Command Line

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
