package at.meks.quarkiverse.axon.shared.model;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.core.unitofwork.ProcessingContext;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;
import org.axonframework.modelling.repository.ManagedEntity;
import org.axonframework.modelling.repository.Repository;

import static java.util.Optional.ofNullable;

@ApplicationScoped
public class DomainServiceExample {

    @Inject
    Repository<String, Giftcard> giftcardRepository;

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
        ofNullable(giftcardAggregate.get().entity()).orElseThrow()
                .requestRedeem(command.amount(), eventAppender);
    }

}
