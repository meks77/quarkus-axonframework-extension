package at.meks.quarkiverse.axon.runtime.health;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.common.configuration.AxonConfiguration;
import org.axonframework.messaging.eventhandling.processing.EventProcessor;
import org.axonframework.messaging.eventhandling.processing.streaming.StreamingEventProcessor;
import org.axonframework.messaging.eventhandling.processing.streaming.segmenting.EventTrackerStatus;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

@ApplicationScoped
@Readiness
public class EventprocessorsHealthCheck implements HealthCheck {

    @Inject
    AxonConfiguration configuration;

    @Override
    public HealthCheckResponse call() {
        var responseBuilder = HealthCheckResponse.builder().name("Axon eventprocessors").up();
        addSubscribingEventprocessorsWithError(responseBuilder);
        return responseBuilder.build();
    }

    private void addSubscribingEventprocessorsWithError(HealthCheckResponseBuilder responseBuilder) {
        Collection<EventProcessor> processorNamesWithErrors = eventprocessors();
        for (EventProcessor processor : processorNamesWithErrors) {
            if (processor.isError()) {
                responseBuilder.down();
                responseBuilder.withData(processor.name(), "shtudown with error");
            } else if (processor instanceof StreamingEventProcessor streamingProcessor) {
                Map<Integer, EventTrackerStatus> segmentsWithError = segmentsWithError(streamingProcessor);
                if (!segmentsWithError.isEmpty()) {
                    responseBuilder.down();
                    addResponseDataForSegments(responseBuilder, streamingProcessor, segmentsWithError);
                }
            }
        }
    }

    private Collection<EventProcessor> eventprocessors() {
        return configuration.getComponents(EventProcessor.class).values();
    }

    private Map<Integer, EventTrackerStatus> segmentsWithError(StreamingEventProcessor processor) {
        return processor.processingStatus().entrySet()
                .stream()
                .filter(e -> e.getValue().isErrorState())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void addResponseDataForSegments(HealthCheckResponseBuilder responseBuilder, StreamingEventProcessor processor,
            Map<Integer, EventTrackerStatus> segmentsWithError) {
        for (Map.Entry<Integer, EventTrackerStatus> errorSegment : segmentsWithError.entrySet()) {
            responseBuilder.withData(
                    "%s; Segment %s".formatted(processor.name(), errorSegment.getKey()),
                    errorSegment.getValue().getError().getMessage());
        }
    }
}
