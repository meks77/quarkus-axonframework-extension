package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.eventhandling.configuration.EventProcessingConfigurer;

/**
 * Interface for configuring tracing within an Axon Framework application.
 * Implementations of this interface allow the integration of detailed
 * tracing configurations into Axon's {@link EventProcessingConfigurer}, enabling distributed
 * tracing or monitoring capabilities.
 */
public interface AxonTracingConfigurer {
    void configureTracing(EventSourcingConfigurer configurer);
}
