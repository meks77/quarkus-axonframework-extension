package at.meks.quarkiverse.axon.tracing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.config.Configurer;
import org.axonframework.tracing.opentelemetry.OpenTelemetrySpanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.meks.quarkiverse.axon.runtime.customizations.AxonTracingConfigurer;
import io.opentelemetry.api.trace.Tracer;

@ApplicationScoped
public class OpenTelemetryConfigurer implements AxonTracingConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(OpenTelemetryConfigurer.class);

    @Inject
    Tracer tracer;

    @Override
    public void configureTracing(Configurer configurer) {
        LOG.info("configure OpenTelemetry tracing");
        configurer.configureSpanFactory(conf -> OpenTelemetrySpanFactory.builder()
                .tracer(tracer)
                .build());
    }

}
