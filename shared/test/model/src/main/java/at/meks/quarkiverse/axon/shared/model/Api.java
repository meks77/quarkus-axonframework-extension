package at.meks.quarkiverse.axon.shared.model;

import org.axonframework.modelling.annotation.TargetEntityId;

public class Api {

    public record IssueCardCommand(@TargetEntityId String id, int initialAmount) {
    }

    public record CardIssuedEvent(String id, int amount) {
    }

    public record RedeemCardCommand(@TargetEntityId String id, int amount) {
    }

    public record CardRedeemedEvent(String id, int amount) {
    }

    public record GiftcardQuery(String id) {
    }

    public record UndoLatestRedemptionCommand(@TargetEntityId String id, int amount) {
    }

    public record LatestRedemptionUndoneEvent(String id, int amount) {
    }

    public record CardGotEmptyEvent(String id) {
    }

    public record ReturnCardCommand(@TargetEntityId String id) {
    }

    public record CardReturnedEvent(String id) {
    }

    public record AddPersonalInformationCommand(@TargetEntityId String id, String personName) {
    }

    public record PersonalInformationAddedEvent(String id, String personName) {
    }
}
