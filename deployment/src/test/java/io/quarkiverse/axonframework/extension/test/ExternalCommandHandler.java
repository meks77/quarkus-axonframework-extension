package io.quarkiverse.axonframework.extension.test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.Aggregate;

import io.quarkiverse.axonframework.extension.runtime.RepositorySupplier;

@ApplicationScoped
public class ExternalCommandHandler {

    @Inject
    RepositorySupplier repositorySupplier;

    @CommandHandler
    void handle(Api.RedeemCardCommand command) {
        Aggregate<Giftcard> giftcardAggregate = repositorySupplier.repository(Giftcard.class).load(command.id());
        giftcardAggregate.execute(giftcard -> giftcard.requestRedeem(command.amount()));
    }

}
