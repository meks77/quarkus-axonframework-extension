package at.meks.quarkiverse.axon.shared.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.axonframework.eventsourcing.annotation.EventSourcedEntity;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;
import org.axonframework.modelling.entity.annotation.EntityMember;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import at.meks.quarkiverse.axon.shared.model.Api.IssueCardCommand;
import io.quarkus.logging.Log;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@EventSourcedEntity
// TODO: uncomment as soon as Snapshots work
//@Snapshotting(afterEvents = 2)
public class Giftcard {

    private String id;
    private int currentAmount;
    private final List<Integer> cardRedemptions = new ArrayList<>();

    @EntityMember
    private final PersonalInformation personalInformation = new PersonalInformation();

    @EntityCreator
    @SuppressWarnings("unused")
    Giftcard() {
        // necesarry for the axon framework
    }

    @CommandHandler
    public static void handle(IssueCardCommand command, EventAppender eventAppender) {
        if (command == null) {
            throw new IllegalArgumentException("command mustn't be null");
        }
        eventAppender.append(new Api.CardIssuedEvent(command.id(), command.initialAmount()));
        Log.infof("new card with the id %s and the initial amount %s was issued", command.id(), command.initialAmount());
    }

    @EventSourcingHandler
    void handle(Api.CardIssuedEvent event) {
        Log.debugf("handling event %s", event);
        this.id = event.id();
        this.currentAmount = event.amount();
    }

    public void requestRedeem(int amount, EventAppender eventAppender) {
        if (this.currentAmount < amount) {
            throw new IllegalArgumentException("amount must be less than current card amount");
        }
        eventAppender.append(new Api.CardRedeemedEvent(id, amount));
        if (currentAmount == 0) {
            eventAppender.append(new Api.CardGotEmptyEvent(id));
        }
    }

    @EventSourcingHandler
    void handle(Api.CardRedeemedEvent event) {
        this.currentAmount -= event.amount();
        cardRedemptions.add(event.amount());
    }

    public void undoRedemption(int amount, EventAppender eventAppender) {
        Optional<Integer> lastestRedeemedAmount = lastestRedeemedAmount();
        if (lastestRedeemedAmount.isEmpty() || lastestRedeemedAmount.get() != amount) {
            throw new IllegalArgumentException("amount must be the lastest redeem amount");
        } else {
            eventAppender.append(new Api.LatestRedemptionUndoneEvent(id, amount));
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

    @CommandHandler
    void handle(Api.ReturnCardCommand command, EventAppender eventAppender) {
        eventAppender.append(new Api.CardReturnedEvent(command.id()));
    }

}
