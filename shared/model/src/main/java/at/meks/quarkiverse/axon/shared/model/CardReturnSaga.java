package at.meks.quarkiverse.axon.shared.model;

import jakarta.inject.Inject;

import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;

import io.quarkus.logging.Log;

@SuppressWarnings("unused")
public class CardReturnSaga {

    @SuppressWarnings("QsPrivateBeanMembersInspection")
    @Inject
    private Paymentservice paymentservice;

    @StartSaga
    @SagaEventHandler(associationProperty = "id", keyName = "cardId")
    void handle(Api.CardGotEmptyEvent event) {
        Log.infof("Saga: %s; card %s got empty event received in Saga", this, event.id());
        paymentservice.preparePayment(event.id());
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "id", keyName = "cardId")
    void handle(Api.CardReturnedEvent event) {
        Log.infof("Saga: %s; card %s returned event received in Saga", this, event.id());
        paymentservice.payDeposit(event.id());
    }

}
