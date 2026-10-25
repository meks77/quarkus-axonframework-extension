# Quarkus Axon framework extension

## Current state

### Support for Axon Framework 4

The extension for Axon Framework 4 is currently used in production environments. Please note that AxonIQ only provides commercial/enterprise support for this framework version.

This version is maintained on the `support/axon4` branch (releases `1.*.*`). It remains in **maintenance mode**:
* Releases focus on critical bug fixes and dependency updates.
* New features are generally not backported.
* If Quarkus 4 is released before the Axon Framework 5 migration is finalized, support for Quarkus 4 will be provided if compatible.

### Support for Axon Framework 5

Work on Axon Framework 5 support is available on the `main` branch (releases `2.*.*`).

* **Status:** Migration is currently on hold.
* **Timeline:** Active development is scheduled to resume in 2027.

### Support for Quarkus 4

Work on the Quarkus 4 upgrade is available on the `quarkus-4.0-upgrade` branch (currently covering only the `main` branch / Axon Framework 5).

* **Current Status:** The build is successful, but JPA-related modules (JPA Event Store and JPA Token Store) are currently disabled because the Persistence 4 API is incompatible with Axon Framework 5.
* **Next Steps:** Quarkus 4 is currently in beta. The upgrade will be finalized and merged into the `main` branch once Quarkus 4 reaches a stable release and JPA compatibility is resolved. Afterwards, Quarkus 4 support will also be prepared for the `support/axon4` branch.

## Goal

The goal of this extension is to provide a solution which

* simplifies the usage of the axon framework
* provides dev services for the event stores
* provides an integration similar to the spring integration

## Supported Axon Framework Versions

Currently, the extension supports Axon Framework 4 and 5.
On the main branch, the AF 5 is used. Releases for AF 5 have the version 2.*.*

On the support/axon4 branch, Axon Framework 4 is used. Releases for AF 4 have the version 1.*.*. 

Since the upgrade to AF 5, the release version for AF 4 has been moved to 1.*.*. 

## Upgrade from Version 0.* or 1.* to version 2.*.
Because I don't know from a lot of users I just created some notes. These notes can be found in the [migration guide](Axoniq-Framework-5_1-migration-guide.adoc).


## Documentation & User Guide
Please follow the [user guide](docs/modules/ROOT/pages/index.adoc) to get started with this extension.

## Current state

### Quarkus Versions
On the main branch, the most recent quarkus version is used.

### Injectable Beans

* EventBus
* EventGateway
* CommandBus
* CommandGateway
* QueryBus
* QueryGateway
* Repository\<ID, T>(e.g. Repository\<String, GiftCard> if GiftCard is an event sourced entity)

### supported message types

* Events
* Commands
* Queries

### auto configuration

#### automatically registered

* event sourced entities
* event handlers
* command handlers
* query handlers

### custom configuration
You can provide your own setup for

* setting up the framework at all but use the scanned classes
* custom transaction manager
* custom event processors
* custom event store
* custom metrics
* custom token store
* custom event sourced entity configuration

### supported event processors

* "simple" subscribing event processor
* persistent streams (currently disabled until AF 5 supports them)
* pooled event processor
* custom setup of event processors

### supported event stores

* Axon Server
* JPA Event Store
* In-Memory Event Store
* Custom Event Store

### supported Token Stores

* JDBC Token Store
* JPA Token Store
* custom setup for Tokenstore

### Interceptors

* command dispatch and handler interceptors
* event dispatch and default handler interceptors

### Exceptions Handling

* wrapping Exceptions into CommandExecutionException
* wrapping Exceptions into QueryExecutionException

### other supported stuff

* transaction handling
* custom setup of transaction handling
* dev service for the axon server
* live reloading
* snapshots(currently not working since upgrade to AF5)
* inject cdi beans into methods annotated with
  * CommandHandler
  * EventHandler
  * QueryHandler
* Upcasters(currently not working since upgrade to AF5)
* custom Jackson Serialization
* Information provided to Dev-UI


#### Live reloading
Live reloading works with a schedule process, which checks if sources changed.
For some reason, when shutting down the axon configuration, it must be wait, that the new configuration works well.

If the wait time is too less, an error can occur that no command handler for your command is available.
If that happens, please try to increase the wait time. For details please read [the config documenation](docs/modules/ROOT/pages/index.adoc) for more details how to configure.


## Usage

Please follow [Extension Documentation](docs/modules/ROOT/pages/index.adoc).

