package at.meks.quarkiverse.axon.runtime;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.config.Configurer;

import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class NoMetricsConfigurer implements AxonMetricsConfigurer {

    @Override
    public void configure(Configurer configurer) {

    }

}