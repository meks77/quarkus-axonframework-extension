package at.meks.quarkiverse.axon.it;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestPath;

import com.google.protobuf.ByteString;

import io.axoniq.axonserver.grpc.event.dcb.GetLastSnapshotRequest;
import io.axoniq.axonserver.grpc.event.dcb.GetLastSnapshotResponse;
import io.axoniq.framework.axonserver.connector.api.AxonServerConnectionManager;

@Path("system")
public class SystemResource {

    @Inject
    AxonServerConnectionManager axonConnectionManager;

    @ConfigProperty(name = "quarkus.axon.server.context")
    String axonContext;

    @GET
    @Path("snapshots/{aggregateId}/count")
    @Produces(MediaType.TEXT_PLAIN)
    public long getSnapshotCount(@RestPath String aggregateId) throws ExecutionException, InterruptedException {
        CompletableFuture<GetLastSnapshotResponse> lastSnapshotFuture = axonConnectionManager.getConnection(
                        axonContext).snapshotChannel()
                .getLastSnapshot(GetLastSnapshotRequest.newBuilder()
                        .setKey(ByteString.copyFromUtf8("giftcard.Giftcard" + aggregateId)).build());
        return lastSnapshotFuture.get().hasSnapshot() ? 1 : 0;
    }
}
