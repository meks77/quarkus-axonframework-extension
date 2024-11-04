package io.quarkiverse.axonframework.extension.test.live.reloading;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkiverse.axonframework.extension.runtime.AxonConfiguration;
import io.quarkus.logging.Log;

/**
 * This rest serivce is necessary for testing purposes, to get the grpc port of the used axon server.
 */
@Path("/config")
public class ConfigResource {

    @Inject
    AxonConfiguration axonConfiguration;

    @GET
    @Path("/axonserverPort")
    @Produces(MediaType.TEXT_PLAIN)
    public int axonserverPort() {
        Log.infof("requesting axon server grpc port");
        return axonConfiguration.server().grpcPort();
    }

}
