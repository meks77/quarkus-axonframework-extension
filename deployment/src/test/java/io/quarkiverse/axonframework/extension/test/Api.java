package io.quarkiverse.axonframework.extension.test;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class Api {

    public record IssueCardCommand(@TargetAggregateIdentifier String id, int initialAmount) {}
    public record CardIssuedEvent(String id, int amount) {}
}
