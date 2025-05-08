package at.meks.quarkiverse.axon.it;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestPath;

@Path("system")
public class SystemResource {

    @Inject
    AxonServerConnectionManager axonConnectionManager;

    @ConfigProperty(name = "quarkus.axon.server.context")
    String axonContext;

    @GET
    @Path("snapshots/{aggregateId}/count")
    @Produces(MediaType.TEXT_PLAIN)
    public long getSnapshotCount(@RestPath String aggregateId) {
        return axonConnectionManager.getConnection(axonContext).eventChannel()
                .loadSnapshots(aggregateId, Long.MAX_VALUE, Integer.MAX_VALUE).asStream().count();
    }
}
