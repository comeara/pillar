# Pillar Upgrades

## Version 2 to Version 3

When upgrading from version 2 to version 3, please consider the following.

### Command Line Interface Change

In version 2, the command line interface required the -e option, which specifies the environment, to come before the
subcommand. The subcommands are ```initialize``` and ```migrate```.

Version 3 places the subcommand as the first argument, followed by all options, ending with the datastore.

For example, in version 2, you might have run the following command to initialize your test keyspace:

    % pillar -e test initialize

In version 3, that command becomes:

    % pillar initialize -e test
    
### Code Repackaging

In version 2, all the Scala classes were packaged in ```com.chrisomeara.pillar```.

In version 3, the cli has been packaged in ```com.chrisomeara.pillar.cli``` and the core of pillar has been moved to
```com.chrisomeara.pillar.core```.

### RPM Build Removal

Version 3 removes the RedHat Package Manager build. If this feature interests you please open a new issue so that we
can discuss how to best re-implement.