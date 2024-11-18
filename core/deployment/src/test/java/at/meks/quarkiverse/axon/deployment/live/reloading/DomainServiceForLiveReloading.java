package at.meks.quarkiverse.axon.deployment.live.reloading;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.axonframework.modelling.command.Aggregate;
import org.axonframework.modelling.command.Repository;

import at.meks.quarkiverse.axon.shared.model.Api;
import at.meks.quarkiverse.axon.shared.model.Giftcard;

@ApplicationScoped
public class DomainServiceForLiveReloading {

    @Inject
    Repository<Giftcard> giftcardRepository;

    @CommandHandler
    void handle(Api.RedeemCardCommand command) {
        Aggregate<Giftcard> giftcardAggregate = giftcardRepository.load(command.id());
        giftcardAggregate.execute(giftcard -> giftcard.requestRedeem(command.amount()));
    }

    @ExceptionHandler
    public void handleAll(Exception exception) {
        throw new CommandExecutionException("wrapped exception in details", exception);
    }

}
