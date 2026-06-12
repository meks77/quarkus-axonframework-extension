package at.meks.quarkiverse.axon.shared.model;

import org.axonframework.eventsourcing.annotation.EventTag;
import org.axonframework.messaging.commandhandling.annotation.Command;
import org.axonframework.messaging.eventhandling.annotation.Event;
import org.axonframework.messaging.queryhandling.annotation.Query;
import org.axonframework.modelling.annotation.TargetEntityId;

public class Api {

    @Command(routingKey = "id")
    public record IssueCardCommand(@TargetEntityId String id, int initialAmount) {
    }

    @Event(name = "CardIssuedEvent")
    public record CardIssuedEvent(@EventTag(key = "Giftcard") String id, int amount) {
    }

    @Command(routingKey = "id")
    public record RedeemCardCommand(@TargetEntityId String id, int amount) {
    }

    @Event(name = "CardRedeemedEvent")
    public record CardRedeemedEvent(@EventTag(key = "Giftcard") String id, int amount) {
    }

    @Query
    public record GiftcardQuery(String id) {
    }

    @Command(routingKey = "id")
    public record UndoLatestRedemptionCommand(@TargetEntityId String id, int amount) {
    }

    @Event(name = "LatestRedemptionUndoneEvent")
    public record LatestRedemptionUndoneEvent(@EventTag(key = "Giftcard") String id, int amount) {
    }

    @Event(name = "CardGotEmptyEvent")
    public record CardGotEmptyEvent(@EventTag(key = "Giftcard") String id) {
    }

    @Command(routingKey = "id")
    public record ReturnCardCommand(@TargetEntityId String id) {
    }

    @Event(name = "CardReturnedEvent")
    public record CardReturnedEvent(@EventTag(key = "Giftcard") String id) {
    }

    @Command(routingKey = "id")
    public record AddPersonalInformationCommand(@TargetEntityId String id, String personName) {
    }

    @Event(name = "PersonalInformationAddedEvent")
    public record PersonalInformationAddedEvent(@EventTag(key = "Giftcard") String id, String personName) {
    }
}
