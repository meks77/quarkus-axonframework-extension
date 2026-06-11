package at.meks.quarkiverse.axon.metrics.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.extension.metrics.micrometer.MetricsConfigurationEnhancer;

import at.meks.quarkiverse.axon.runtime.customizations.AxonMetricsConfigurer;
import io.micrometer.core.instrument.MeterRegistry;

@ApplicationScoped
public class MicrometerMetricsConfigurer implements AxonMetricsConfigurer {

    @Inject
    MeterRegistry meterRegistry;

    @Inject
    AxonMetricsConfiguration metricsConfiguration;

    @Override
    public void configure(EventSourcingConfigurer configurer) {
        if (metricsConfiguration.enabled()) {
            MetricsConfigurationEnhancer metricsEnhancer = new MetricsConfigurationEnhancer(meterRegistry,
                    metricsConfiguration.useDimensions());
            configurer.componentRegistry(cr -> cr.registerEnhancer(metricsEnhancer));
        }
    }
}

