package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Reserve;

import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;

import at.meks.quarkiverse.axon.runtime.customizations.AxonMetricsConfigurer;

@ApplicationScoped
@Reserve
@Priority(0)
public class NoMetricsConfigurer implements AxonMetricsConfigurer {

    @Override
    public void configure(EventSourcingConfigurer configurer) {

    }

}
