package at.meks.quarkiverse.axonframework.example.interfaces.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.axonframework.queryhandling.QueryGateway;
import org.jboss.resteasy.reactive.RestQuery;

import at.meks.quarkiverse.axonframework.example.projection.GiftcardQuery;
import at.meks.quarkiverse.axonframework.example.projection.GiftcardView;
import io.smallrye.mutiny.Uni;

@Path("giftcard")
public class GiftcardResource {

    @Inject
    QueryGateway queryGateway;

    @GET
    public Uni<GiftcardView> getGiftcard(@RestQuery String id) {
        return Uni.createFrom()
                .future(() -> queryGateway.query(new GiftcardQuery(id), GiftcardView.class));
    }

}
