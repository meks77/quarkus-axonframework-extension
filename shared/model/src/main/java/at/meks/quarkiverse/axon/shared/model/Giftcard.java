package at.meks.quarkiverse.axon.shared.model;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.axonframework.modelling.command.AggregateIdentifier;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import at.meks.quarkiverse.axon.shared.model.Api.IssueCardCommand;
import io.quarkus.logging.Log;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
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
    Giftcard(IssueCardCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command mustn't be null");
        }
        apply(new Api.CardIssuedEvent(command.id(), command.initialAmount()));
        Log.infof("new card with the id %s and the initial amount %s was issued", command.id(), currentAmount);
    }

    @EventSourcingHandler
    void handle(Api.CardIssuedEvent event) {
        Log.debugf("handling event %s", event);
        this.id = event.id();
        this.currentAmount = event.amount();
    }

    public void requestRedeem(int amount) {
        if (this.currentAmount < amount) {
            throw new IllegalArgumentException("amount must be less than current card amount");
        }
        apply(new Api.CardRedeemedEvent(id, amount));
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
