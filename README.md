# Quarkus Axon framework extension

## Goal

The goal of this extension is to provide a solution which 

* simplifies the usage of the axon framework
* provides dev services for the event stores
* provides an integration similar to the spring integration

## Current state

* Event and command gateways and bus can be injected
* Events and Commands can be published
* a connection to the axon server is created
* a dev service for the axon server is started
* aggregates are automatically registered
* event handlers are automatically registered

## Future features
The extension is just at the beginning. In the project tab are some of the features, which are currently planned.

While implementing, new features will be created and the order of the features can change.

Help for implementing the features is welcome.