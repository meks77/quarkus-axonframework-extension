# Quarkus Axon framework extension

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
* Repository\<T>(e.g. Repository\<GiftCard> if GiftCard is a aggregate)

### supported message types

* Events
* Commands
* Queries

### auto configuration

#### automatically registered

* aggregates
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
* custom aggregate configuration

### supported event processors

#### Subscribing processors

* "simple" subscribing event processor
* persistent streams
* tracking event processor
* pooled event processor
* custom setup of event processors

### supported event stores

* Axon Server
* JPA Aggregate Based Event Store
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

