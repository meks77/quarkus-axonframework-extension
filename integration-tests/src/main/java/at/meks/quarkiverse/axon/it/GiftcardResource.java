package at.meks.quarkiverse.axon.it;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import at.meks.quarkiverse.axon.shared.model.Api;
import at.meks.quarkiverse.axon.shared.projection.GiftcardView;
import io.smallrye.mutiny.Uni;

@Path("giftcard")
public class GiftcardResource {

    @Inject
    QueryGateway queryGateway;

    @Inject
    CommandGateway commandGateway;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<GiftcardView> getGiftcard(@RestQuery String id) {
        return Uni.createFrom()
                .future(() -> queryGateway.query(new Api.GiftcardQuery(id), GiftcardView.class));
    }

    @POST
    @Path("{cardId}/{initialAmount}")
    public Response issueCard(@RestPath String cardId, @RestPath int initialAmount) {
        try {
            commandGateway.sendAndWait(new Api.IssueCardCommand(cardId, initialAmount));
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(toResponseEntity(e)).build();
        }
    }

    private static String toResponseEntity(Exception e) {
        if (e.getCause() == null) {
            return e.getMessage();
        }
        return e.getMessage() + "; " + e.getCause().getMessage();
    }

    @PUT
    @Path("{cardId}/{amount}")
    public Response redeem(@RestPath String cardId, @RestPath int amount) {
        try {
            commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId, amount));
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(toResponseEntity(e)).build();
        }
    }

    @DELETE
    @Path("{cardId}/{amount}")
    public Response undoRedemption(@RestPath String cardId, @RestPath int amount) {
        try {
            commandGateway.sendAndWait(new Api.UndoLatestRedemptionCommand(cardId, amount));
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(toResponseEntity(e)).build();
        }
    }
}
