package at.meks.quarkiverse.axon.shared.model;

import jakarta.inject.Inject;

import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import io.quarkus.logging.Log;

@SuppressWarnings("unused")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CardReturnSaga {

    @SuppressWarnings("QsPrivateBeanMembersInspection")
    @Inject
    private transient Paymentservice paymentservice;

    private CardState state;

    @StartSaga
    @SagaEventHandler(associationProperty = "id", keyName = "cardId")
    void handle(Api.CardGotEmptyEvent event) {
        Log.infof("Saga: %s; card %s got empty event received in Saga", this, event.id());
        paymentservice.preparePayment(event.id());
        state = CardState.waitForReturn;
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "id", keyName = "cardId")
    void handle(Api.CardReturnedEvent event) {
        Log.infof("Saga: %s; card %s returned event received in Saga", this, event.id());
        paymentservice.payDeposit(event.id());
        state = CardState.cardReceived;
    }

    private enum CardState {
        waitForReturn,
        cardReceived
    }
}
