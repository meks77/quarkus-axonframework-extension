package at.meks.quarkiverse.axonframework.example.projection;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;

import at.meks.quarkiverse.axonframework.example.model.Api;

@ApplicationScoped
public class GiftcardQueryHandler {

    private final Map<String, GiftcardView> giftcards = new HashMap<>();

    @QueryHandler
    GiftcardView handle(GiftcardQuery query) {
        return giftcards.get(query.id());
    }

    @EventHandler
    void handle(Api.CardIssuedEvent event) {
        giftcards.put(event.id(), new GiftcardView(event.id(), event.amount()));
    }

    @EventHandler
    void handle(Api.CardRedeemedEvent event) {
        giftcards.get(event.id()).redeem(event.amount());
    }

    @EventHandler
    void handle(Api.LatestRedemptionUndoneEvent event) {
        giftcards.get(event.id()).undoLatestRedemption(event.amount());
    }
}
