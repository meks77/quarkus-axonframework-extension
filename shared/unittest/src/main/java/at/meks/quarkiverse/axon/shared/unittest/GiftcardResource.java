package at.meks.quarkiverse.axon.shared.unittest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.jboss.resteasy.reactive.RestPath;

import at.meks.quarkiverse.axon.shared.model.Api;
import at.meks.quarkiverse.axon.shared.projection.GiftcardView;
import io.smallrye.mutiny.Uni;

@Path("/giftcard")
public class GiftcardResource {

    @Inject
    CommandGateway commandGateway;

    @Inject
    QueryGateway queryGateway;

    @POST
    @Path("{cardId}/{initialBalance}")
    public void createGiftcard(@RestPath String cardId, @RestPath int initialBalance) {
        commandGateway.sendAndWait(new Api.IssueCardCommand(cardId, initialBalance));
    }

    @PUT
    @Path("{cardId}/{amount}")
    public void redeemCard(@RestPath String cardId, @RestPath int amount) {
        commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId, amount));
    }

    @GET
    @Path("{cardId}")
    @Produces("text/plain")
    public Uni<Integer> currentAmount(@RestPath String cardId) {
        return Uni.createFrom()
                .future(() -> queryGateway.query(new Api.GiftcardQuery(cardId), GiftcardView.class))
                .map(GiftcardView::getCurrentAmount);
    }
}
