package at.meks.quarkiverse.axon.example.model;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.axonframework.modelling.command.AggregateIdentifier;

import io.quarkus.logging.Log;

public class Giftcard {

    @AggregateIdentifier
    private String id;
    private int currentAmount;
    private final List<Integer> cardRedemptions = new ArrayList<>();

    @SuppressWarnings("unused")
    Giftcard() {
        // necesarry for the axon framework
    }

    @CommandHandler
    Giftcard(Api.IssueCardCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command mustn't be null");
        }
        apply(new Api.CardIssuedEvent(command.id(), command.initialAmount()));
        Log.infof("new card with the id %s and the initial amount %s was issued", command.id(), command.initialAmount());
    }

    @EventSourcingHandler
    void handle(Api.CardIssuedEvent event) {
        this.id = event.id();
        this.currentAmount = event.amount();
    }

    @CommandHandler
    void handle(Api.RedeemCardCommand command) {
        if (this.currentAmount < command.amount()) {
            throw new IllegalArgumentException(
                    "amount(" + command.amount() + ") must be less than current card amount(" + currentAmount + ")");
        }
        apply(new Api.CardRedeemedEvent(id, command.amount()));
        Log.infof("card was redeemed by %s", command.amount());
    }

    @EventSourcingHandler
    void handle(Api.CardRedeemedEvent event) {
        this.currentAmount -= event.amount();
        cardRedemptions.add(event.amount());
    }

    public void undoRedemption(int amount) {
        Optional<Integer> lastestRedeemedAmount = lastestRedeemedAmount();
        if (lastestRedeemedAmount.isEmpty() || lastestRedeemedAmount.get() != amount) {
            throw new IllegalArgumentException("amount must be the lastest redeem amount");
        } else {
            apply(new Api.LatestRedemptionUndoneEvent(id, amount));
            Log.infof("latest redemption was undone");
        }
    }

    private Optional<Integer> lastestRedeemedAmount() {
        if (!cardRedemptions.isEmpty()) {
            return Optional.of(cardRedemptions.get(cardRedemptions.size() - 1));
        }
        return Optional.empty();
    }

    @EventSourcingHandler
    void handle(Api.LatestRedemptionUndoneEvent event) {
        cardRedemptions.remove(cardRedemptions.size() - 1);
        this.currentAmount += event.amount();
    }

    @ExceptionHandler
    public void handleAll(Exception exception) {
        throw new CommandExecutionException("wrapped exception in details", exception);
    }

}
