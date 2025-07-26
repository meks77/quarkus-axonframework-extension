package at.meks.quarkiverse.axon.tracing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.config.Configurer;
import org.axonframework.tracing.opentelemetry.OpenTelemetrySpanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.meks.quarkiverse.axon.runtime.customizations.AxonTracingConfigurer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;

@ApplicationScoped
public class OpenTelemetryConfigurer implements AxonTracingConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(OpenTelemetryConfigurer.class);

    @Inject
    Instance<Tracer> tracer;

    @Inject
    Instance<OpenTelemetry> openTelemetry;

    @Override
    public void configureTracing(Configurer configurer) {
        if (openTelemetry.isResolvable() && tracer.isResolvable() && openTelemetry.get() != null) {
            LOG.info("configure OpenTelemetry tracing");
            configurer.configureSpanFactory(conf -> OpenTelemetrySpanFactory.builder()
                    .tracer(tracer.get())
                    .build());
        } else {
            LOG.info("OpenTelemetry tracing is deactivated");
        }
    }

}
