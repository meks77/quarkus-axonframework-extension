package io.quarkiverse.axonframework.extension.test;

import java.util.*;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;

import io.quarkiverse.axonframework.extension.test.model.Api;

@ApplicationScoped
public class GiftcardQueryHandler {

    private final Map<String, GiftcardView> giftcards = new HashMap<>();

    @QueryHandler
    GiftcardView handle(Api.GiftcardQuery query) {
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
}
