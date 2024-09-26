package io.quarkiverse.axonframework.extension.test;

import io.quarkiverse.axonframework.extension.test.Api.IssueCardCommand;
import io.quarkus.logging.Log;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.AggregateIdentifier;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

public class Giftcard {

    @AggregateIdentifier
    private final String id;
    private final int currentAmount;

    @SuppressWarnings("unused")
    Giftcard() {
        // necesarry for the axon framework
        id = null;
        currentAmount = 0;
    }

    @CommandHandler
    Giftcard(IssueCardCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command mustn't be null");
        }
        this.id = command.id();
        this.currentAmount = command.initialAmount();
        apply(new Api.CardIssuedEvent(id, currentAmount));
        Log.infof("new card with the id %s and the initial amount %s was issued", id, currentAmount);
    }

}
