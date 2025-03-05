package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.config.Configurer;

/**
 * Interface for configuring tracing within an Axon Framework application.
 * Implementations of this interface allow the integration of detailed
 * tracing configurations into Axon's {@link Configurer}, enabling distributed
 * tracing or monitoring capabilities.
 */
public interface AxonTracingConfigurer {
    void configureTracing(Configurer configurer);
}
