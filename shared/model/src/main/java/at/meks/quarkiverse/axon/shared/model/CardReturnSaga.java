package at.meks.quarkiverse.axon.shared.model;

import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;

import io.quarkus.logging.Log;

public class CardReturnSaga {

    @StartSaga
    @SagaEventHandler(associationProperty = "id", keyName = "cardId")
    void handle(Api.CardGotEmptyEvent event) {
        Log.infof("Saga: %s; card %s got empty event received in Saga", this, event.id());
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "id", keyName = "cardId")
    void handle(Api.CardReturnedEvent event) {
        Log.infof("Saga: %s; card %s returned event received in Saga", this, event.id());
    }

}
