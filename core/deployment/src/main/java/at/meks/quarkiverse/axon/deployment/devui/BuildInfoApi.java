package at.meks.quarkiverse.axon.deployment.devui;

import java.util.List;

public class BuildInfoApi {

    record EventSourcedEntity(String className) {
    }

    record SagaEventHandler(String className) {
    }

    record EventHandler(String className) {
    }

    public record QueryHandler(String className) {
    }

    public record CommandHandler(String className) {
    }

    record BuildInfo(List<EventSourcedEntity> eventSourcedEntities, List<SagaEventHandler> sagaEventHandlers,
            List<EventHandler> eventHandlers,
            List<QueryHandler> queryHandlers, List<CommandHandler> commandHandlers) {

    }
}
