package at.meks.quarkiverse.axon.metrics.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.config.Configurer;
import org.axonframework.micrometer.GlobalMetricRegistry;

import at.meks.quarkiverse.axon.runtime.customizations.AxonMetricsConfigurer;
import io.micrometer.core.instrument.MeterRegistry;

@ApplicationScoped
public class MicrometerMetricsConfigurer implements AxonMetricsConfigurer {

    @Inject
    MeterRegistry meterRegistry;

    @Inject
    AxonMetricsConfiguration metricsConfiguration;

    @Override
    public void configure(Configurer configurer) {
        if (metricsConfiguration.enabled()) {
            GlobalMetricRegistry globalMetricRegistry = new GlobalMetricRegistry(meterRegistry);
            if (metricsConfiguration.withTags()) {
                globalMetricRegistry.registerWithConfigurerWithDefaultTags(configurer);
            } else {
                globalMetricRegistry.registerWithConfigurer(configurer);
            }
        }
    }

}
