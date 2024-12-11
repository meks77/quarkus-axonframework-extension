# Quarkus Axon framework extension

## Goal

The goal of this extension is to provide a solution which

* simplifies the usage of the axon framework
* provides dev services for the event stores
* provides an integration similar to the spring integration

## Documentation & User Guide
Please follow the [user guide](docs/modules/ROOT/pages/index.adoc) to get started with this extension.

## Current state

### Quarkus Versions
On the main branch, the most recent quarkus version is used.
To also support the latest LTS version of quarkus, a release branch for this LTS version is also maintained.

Currently, the LTS version 3.15 is supported in the branch release/quarkus-3.15

### Injectable Beans

* EventBus
* EventGateway
* CommandBus
* CoammandGateway
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
* JPA Event Store
* JDBC Event Store
* Custom Event Store

### supported Token Stores

* JDBC Token Store
* custom setup for Tokenstore

### other supported stuff

* transaction handling
* custom setup of transaction handling
* dev service for the axon server
* live reloading
* snapshots
* command dispatch interceptors
* command handler interceptors

#### Live reloading
Live reloading works with a schedule process, which checks if sources changed.
For some reason, when shutting down the axon configuration, it must be wait, that the new configuration works well.

If the wait time is too less, an error can occur that no command handler for your command is available.
If that happens, please try to increase the wait time. For details please read [the config documenation](docs/modules/ROOT/pages/index.adoc) for more details how to configure.

#### Live reloading
Live reloading works with a schedule process, which checks if sources changed.
For some reason, when shutting down the axon configuration, it must be wait, that the new configuration works well.

If the wait time is too less, an error can occur that no command handler for your command is available.
If that happens, please try to increase the wait time. For details please read [the config documenation](docs/modules/ROOT/pages/index.adoc) for more details how to configure.

## Future features

The extension is just at the beginning. In the project tab are some of the features, which are currently planned.

While implementing, new features will be created and the order of the features can change.

Help for implementing the features is welcome.

## Usage

Please follow [Extension Documentation](docs/modules/ROOT/pages/index.adoc).

