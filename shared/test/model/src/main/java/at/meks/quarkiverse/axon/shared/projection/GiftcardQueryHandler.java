package at.meks.quarkiverse.axon.shared.projection;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;

import at.meks.quarkiverse.axon.shared.model.Api;

@ApplicationScoped
public class GiftcardQueryHandler {

    private final Map<String, GiftcardView> giftcards = new HashMap<>();

    private boolean cardIssuedEventWasHandled = false;

    @QueryHandler
    GiftcardView handle(Api.GiftcardQuery query) {
        return giftcards.get(query.id());
    }

    @EventHandler
    void handle(Api.CardIssuedEvent event) {
        cardIssuedEventWasHandled = true;
        giftcards.put(event.id(), new GiftcardView(event.id(), event.amount()));
    }

    @EventHandler
    void handle(Api.PersonalInformationAddedEvent event) {
        giftcards.get(event.id()).setPersonName(event.personName());
    }

    @EventHandler
    void handle(Api.CardRedeemedEvent event) {
        giftcards.get(event.id()).redeem(event.amount());
    }

    @EventHandler
    void handle(Api.LatestRedemptionUndoneEvent event) {
        giftcards.get(event.id()).undoLastRedemption(event.amount());
    }

    public boolean cardIssuedEventWasHandled() {
        return cardIssuedEventWasHandled;
    }
}
