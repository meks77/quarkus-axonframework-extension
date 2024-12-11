package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.config.Configurer;

/**
 * This interface defines a contract for configuring axon-specific metrics.
 * Implementations of this interface can apply custom configurations 
 * to enhance the monitoring capabilities of AxonFramework-based applications.
 */
public interface AxonMetricsConfigurer {

    /**
     * Configures the given {@link Configurer} instance to enable or customize metric tracking.
     *
     * @param configurer the Axon {@link Configurer} instance to be customized
     */
    void configure(Configurer configurer);

}
