package io.quarkiverse.axonframework.extension.test.model;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.Aggregate;
import org.axonframework.modelling.command.Repository;

@ApplicationScoped
public class DomainServiceExample {

    @Inject
    Repository<Giftcard> giftcardRepository;

    @CommandHandler
    void handle(Api.RedeemCardCommand command) {
        Aggregate<Giftcard> giftcardAggregate = giftcardRepository.load(command.id());
        giftcardAggregate.execute(giftcard -> giftcard.requestRedeem(command.amount()));
    }

}
