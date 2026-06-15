package at.meks.quarkiverse.axon.deployment.live.reloading;

import java.util.concurrent.ExecutionException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.messaging.commandhandling.CommandExecutionException;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.core.interception.annotation.ExceptionHandler;
import org.axonframework.messaging.core.unitofwork.ProcessingContext;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;
import org.axonframework.modelling.repository.ManagedEntity;
import org.axonframework.modelling.repository.Repository;

import at.meks.quarkiverse.axon.shared.model.Api;
import at.meks.quarkiverse.axon.shared.model.Giftcard;

@ApplicationScoped
public class DomainServiceForLiveReloading {

    @Inject
    Repository<String, Giftcard> giftcardRepository;

    @CommandHandler
    void handle(Api.RedeemCardCommand command, ProcessingContext processingContext, EventAppender eventAppender)
            throws ExecutionException, InterruptedException {
        ManagedEntity<String, Giftcard> giftcardAggregate = giftcardRepository.load(command.id(), processingContext).get();
        giftcardAggregate.applyStateChange(giftcard -> {
            giftcard.requestRedeem(command.amount(), eventAppender);
            return giftcard;
        });
    }

    @ExceptionHandler
    public void handleAll(Exception exception) {
        throw new CommandExecutionException("wrapped exception in details", exception);
    }

}
