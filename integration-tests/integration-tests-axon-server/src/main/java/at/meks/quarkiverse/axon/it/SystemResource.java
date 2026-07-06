package at.meks.quarkiverse.axon.it;

import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.eventsourcing.snapshot.api.Snapshot;
import org.axonframework.eventsourcing.snapshot.store.SnapshotStore;
import org.axonframework.messaging.core.QualifiedName;
import org.jboss.resteasy.reactive.RestPath;

@Path("system")
public class SystemResource {


    @Inject
    Configuration configuration;


    @GET
    @Path("snapshots/{aggregateId}/count")
    @Produces(MediaType.TEXT_PLAIN)
    public long getSnapshotCount(@RestPath String aggregateId) throws ExecutionException, InterruptedException {
        Snapshot lastSnapshotFuture = configuration.getComponent(
                SnapshotStore.class).load(new QualifiedName("giftcard.Giftcard"), aggregateId).get();
        return lastSnapshotFuture != null ? 1 : 0;
    }

}
