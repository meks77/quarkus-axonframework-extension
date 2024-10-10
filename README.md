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

### supported event stores
* axon server

### dev services
* for the axon server

## Future features
The extension is just at the beginning. In the project tab are some of the features, which are currently planned.

While implementing, new features will be created and the order of the features can change.

Help for implementing the features is welcome.

## Usage

Currently, it is not published to a maven repo, because it is in a too early stage. As long as it doesn't have a minimum functionality, it will not be published.

Because of that you have to check out the sources and install it to your local maven repository using the following command:

```shell
mvn install
```

If you have a container engine installed (e.g. docker or podman), the build will start an axon server and run tests. If containers are not supported, you must start an axon server and configure the grpc port, if it is not equal to the default.

After the successfull build you follow [Extension Documentation](docs/modules/ROOT/pages/index.adoc). Be aware of the wrong version in this documentation. Currently the Version is 999-SNAPSHOT.

### Provided CDI Beans

The following Axon Framework Type can simply be injected:   

* EventGateway
* EventBus
* CommandGateway and
* CommandBus

### Aggregates, event handler and command handler

Aggregates, event handler and command handler are detected automatically.

### Event store

As Eventstore the Axon Server is used. The configuration for the connection is described in the [Extension Documentation](docs/modules/ROOT/pages/index.adoc)

### Repositories

Currently the Bean RepositorySupplier can be injected, which provides access to the repositories for aggregates. 

### Dev service for the Axon Server

If dev services are enabled and you have a container engine running(e.g. docker or podman), an axon server is automatically started, when starting the application in dev mode.