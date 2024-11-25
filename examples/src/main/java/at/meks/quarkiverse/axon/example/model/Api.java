package at.meks.quarkiverse.axon.example.model;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class Api {

    public record IssueCardCommand(@TargetAggregateIdentifier String id, int initialAmount) {
    }

    public record CardIssuedEvent(String id, int amount) {
    }

    public record RedeemCardCommand(@TargetAggregateIdentifier String id, int amount) {
    }

    public record CardRedeemedEvent(String id, int amount) {
    }

    public record UndoLatestRedemptionCommand(@TargetAggregateIdentifier String id, int amount) {
    }

    public record LatestRedemptionUndoneEvent(String id, int amount) {
    }
}
