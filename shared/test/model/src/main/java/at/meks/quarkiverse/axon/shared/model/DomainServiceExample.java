package at.meks.quarkiverse.axon.shared.model;

import static java.util.Optional.ofNullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.core.unitofwork.ProcessingContext;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;
import org.axonframework.modelling.repository.ManagedEntity;
import org.axonframework.modelling.repository.Repository;

@ApplicationScoped
public class DomainServiceExample {

    private final Repository<String, Giftcard> giftcardRepository;

    public DomainServiceExample(Repository<String, Giftcard> giftcardRepository) {
        this.giftcardRepository = giftcardRepository;
    }

    @CommandHandler
    void handle(Api.UndoLatestRedemptionCommand command, ProcessingContext processingContext, EventAppender eventAppender)
            throws ExecutionException, InterruptedException {
        CompletableFuture<ManagedEntity<String, Giftcard>> giftcardAggregate = giftcardRepository.load(command.id(),
                processingContext);
        ofNullable(giftcardAggregate.get().entity()).orElseThrow()
                .undoRedemption(command.amount(), eventAppender);
    }

    @CommandHandler
    void handle(Api.RedeemCardCommand command, ProcessingContext processingContext, EventAppender eventAppender)
            throws ExecutionException, InterruptedException {
        CompletableFuture<ManagedEntity<String, Giftcard>> giftcardAggregate = giftcardRepository.load(command.id(),
                processingContext);
        ofNullable(giftcardAggregate.get().entity())
                .orElseThrow(() -> new IllegalStateException("The aggregate was not found in the event store"))
                .requestRedeem(command.amount(), eventAppender);
    }

}
