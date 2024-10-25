# Quarkus Axon framework extension

## Goal

The goal of this extension is to provide a solution which

* simplifies the usage of the axon framework
* provides dev services for the event stores
* provides an integration similar to the spring integration

## Current state

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

### supported event processors

#### Subscribing processors

* "simple" subscribing event processor
* persistent streams
* tracking event processor
* pooled event processor

### supported event stores

* axon server

### other supported stuff

* transaction handling
* JDBC token store

### dev services

* for the axon server

## Future features

The extension is just at the beginning. In the project tab are some of the features, which are currently planned.

While implementing, new features will be created and the order of the features can change.

Help for implementing the features is welcome.

## Usage

Currently, it is not published to a maven repo, because it is in a too early stage. As long as it doesn't have a minimum
functionality, it will not be published.

Because of that you have to check out the sources and install it to your local maven repository using the following
command:

```shell
mvn install
```

If you have a container engine installed (e.g. docker or podman), the build will start an axon server and run tests. If
containers are not supported, you must start an axon server and configure the grpc port, if it is not equal to the
default.

After the successfull build you follow [Extension Documentation](docs/modules/ROOT/pages/index.adoc). Be aware of the
wrong version in this documentation. Currently the Version is 999-SNAPSHOT.

### Provided CDI Beans

The following Axon Framework Type can simply be injected:

* EventGateway
* EventBus
* CommandGateway and
* CommandBus

### Aggregates, event handler and command handler

Aggregates, event handler and command handler are detected automatically.

### Event store

As Eventstore the Axon Server is used. The configuration for the connection is described in
the [Extension Documentation](docs/modules/ROOT/pages/index.adoc)

### Repositories

Currently the Bean RepositorySupplier can be injected, which provides access to the repositories for aggregates.

### Dev service for the Axon Server

If dev services are enabled and you have a container engine running(e.g. docker or podman), an axon server is
automatically started, when starting the application in dev mode.

### Event Processors

#### Default event processors

##### Subscribing Event Processors

The simple subscribing event processor is enabled by default. There is nothing you need to do, to have the subscribing
event processor active.

Please read [the config documenation](docs/modules/ROOT/pages/index.adoc) for more details how to configure.

##### Persistent Streams

If you want to use persistent streams by default, configure

```
quarkus.axon.eventhandling.default-mode=persistent-stream
```

You can also configure

* the initial position
* the initial segments
* the stream name
* the message source name
* the used context
* the filter and
* the batch for processing the events

For more details for the configuration please read
the [configurations documentation](docs/modules/ROOT/pages/index.adoc)

Please read [the config documenation](docs/modules/ROOT/pages/index.adoc) for more details how to configure.

##### Tracking event processor

If you want to use the tracking event processor by default, configure

```
quarkus.axon.eventhandling.default-mode=tracking
```

You can also configure

* the initial position
* the initial segments
* the batch size
* the thread-count
* token claim interval
* token claim time unit

Please read [the config documenation](docs/modules/ROOT/pages/index.adoc) for more details how to configure.

##### Pooled event processor

If you want to use the pooled event processor by default, configure

```
quarkus.axon.eventhandling.default-mode=pooled
```

You can also configure

* the initial position
* the initial segments
* the batch size
* the stream name
* the max claimed segments
* if the coordinator claim extension should be activated

Please read [the config documenation](docs/modules/ROOT/pages/index.adoc) for more details how to configure.

### Transaction Management

If quarkus transaction management is available, transaction management for the axon framework is activated automatically

### Token stores
Currently, 2 different token stores are supported.

* in memory
* jdbc

To configure the token store using the config key `quarkus.axon.eventhandling.default-streaming-processor.tokenstore.type`.

Please read [the config documenation](docs/modules/ROOT/pages/index.adoc) for more details how to configure. 