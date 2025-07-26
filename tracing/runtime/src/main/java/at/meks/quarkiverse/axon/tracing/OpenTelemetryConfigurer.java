package at.meks.quarkiverse.axon.tracing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
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
    Instance<Tracer> tracer;

    @Override
    public void configureTracing(Configurer configurer) {
        if (tracer.isResolvable()) {
            LOG.info("configure OpenTelemetry tracing");
            configurer.configureSpanFactory(conf -> {
                giveOpentelemtryAChangeToInitialize();
                return OpenTelemetrySpanFactory.builder()
                        .tracer(tracer.get())
                        .build();
            });
        } else {
            LOG.info("OpenTelemetry tracing is deactivated");
        }
    }

    /**
     * if the configuration of the framework is to fast, it seems that a concurrency problem sometimes leads to the error:
     * GlobalOpenTelemetry.set has already been called. GlobalOpenTelemetry.set must be called only once before any calls to GlobalOpenTelemetry.get. If you are using the OpenTelemetrySdk, use OpenTelemetrySdkBuilder.buildAndRegisterGlobal instead. Previous invocation set to cause of this exception.
     * <p>
     * Currently, the error occured when execution the tests. But I am not sure if it doesn't occur in production too.
     * <p>
     * The cause has the following stack:
     * Caused by: java.lang.Throwable
     * 	at io.opentelemetry.api.GlobalOpenTelemetry.set(GlobalOpenTelemetry.java:115)
     * 	at io.opentelemetry.api.GlobalOpenTelemetry.get(GlobalOpenTelemetry.java:85)
     * 	at io.opentelemetry.api.GlobalOpenTelemetry.getPropagators(GlobalOpenTelemetry.java:217)
     * 	at org.axonframework.tracing.opentelemetry.OpenTelemetrySpanFactory$Builder.build(OpenTelemetrySpanFactory.java:291)
     * 	at at.meks.quarkiverse.axon.tracing.OpenTelemetryConfigurer.lambda$configureTracing$0(OpenTelemetryConfigurer.java:31)
     * 	at org.axonframework.config.Component.get(Component.java:85)
     * 	at org.axonframework.config.DefaultConfigurer$ConfigurationImpl.getComponent(DefaultConfigurer.java:1139)
     * 	at org.axonframework.config.Configuration.getComponent(Configuration.java:278)
     * <p>
     * As far as I can see it the error happens in the opentelemetry source in at io.opentelemetry.api.GlobalOpenTelemetry.get
     * at the release 1.44.1 in the line 85 when setting Opentelemtry.noop() as instance. But why should it be noop when it's activated.
     * <p>
     * The source of Opentelemetry seems to be thread-safe. Maybe the Quarkus Extension isn't. Maybe sometimes it's initialized to late.
     * Using a wait time of 200 ms solved the problem on my machine.
     *
     */
    private static void giveOpentelemtryAChangeToInitialize() {
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
