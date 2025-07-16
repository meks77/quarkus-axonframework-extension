package at.meks.quarkiverse.axon.deployment.devui;

import java.util.List;

public class BuildInfoApi {

    record Aggregate(String className) {
    }

    record SagaEventHandler(String className) {
    }

    record EventHandler(String className) {
    }

    public record QueryHandler(String className) {
    }

    record BuildInfo(List<Aggregate> aggregates, List<SagaEventHandler> sagaEventHandler, List<EventHandler> eventHandler,
            List<QueryHandler> queryHandler) {

    }
}
