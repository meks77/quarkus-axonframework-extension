package io.quarkiverse.axonframework.extension.test.model;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;

import io.quarkiverse.axonframework.extension.test.model.Api.IssueCardCommand;
import io.quarkus.logging.Log;

public class Giftcard {

    @AggregateIdentifier
    private String id;
    private int currentAmount;

    @SuppressWarnings("unused")
    Giftcard() {
        // necesarry for the axon framework
    }

    @CommandHandler
    Giftcard(IssueCardCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command mustn't be null");
        }
        apply(new Api.CardIssuedEvent(command.id(), command.initialAmount()));
        Log.infof("new card with the id %s and the initial amount %s was issued", command.id(), currentAmount);
    }

    @EventSourcingHandler
    public void handle(Api.CardIssuedEvent event) {
        Log.infof("handling event %s", event);
        this.id = event.id();
        this.currentAmount = event.amount();
    }

    public void requestRedeem(int amount) {
        if (this.currentAmount < amount) {
            throw new IllegalArgumentException("amount must be greater than current amount");
        }
        apply(new Api.CardRedeemedEvent(id, amount));
    }
}
