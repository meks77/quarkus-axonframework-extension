package at.meks.quarkiverse.axon.eventprocessor.pooled.runtime;

import java.util.Collection;
import java.util.concurrent.Executors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.common.AxonThreadFactory;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.messaging.StreamableMessageSource;

import at.meks.quarkiverse.axon.eventprocessors.shared.TokenBuilder;
import at.meks.quarkiverse.axon.runtime.customizations.AxonEventProcessingConfigurer;

@ApplicationScoped
public class PooledEventProcessingConfigurer implements AxonEventProcessingConfigurer {

    @Inject
    PooledProcessorConf pooledProcessorConf;

    @Override
    public void configure(EventProcessingConfigurer configurer, Collection<Object> eventhandlers) {
        configurer.usingPooledStreamingEventProcessors();

        configurer.registerPooledStreamingEventProcessorConfiguration(createProcessorConfig(
                pooledProcessorConf.eventprocessorConfigs().get("default"), null));

        pooledProcessorConf.eventprocessorConfigs().entrySet().stream()
                .filter(e -> !e.getKey().equals("default"))
                .forEach(e -> configurer.registerPooledStreamingEventProcessorConfiguration(e.getKey(),
                        createProcessorConfig(e.getValue(), e.getKey())));
    }

    private EventProcessingConfigurer.PooledStreamingProcessorConfiguration createProcessorConfig(
            PooledProcessorConf.ConfigOfOneProcessor configOfOneProcessor, String name) {
        return (config, builder) -> {
            builder.initialToken(messageSource -> initialToken(messageSource, configOfOneProcessor));
            builder.tokenStore(config.getComponent(TokenStore.class));
            configOfOneProcessor.batchSize()
                    .filter(size -> size > 0)
                    .ifPresent(builder::batchSize);
            configOfOneProcessor.initialSegments()
                    .filter(segments -> segments > 0)
                    .ifPresent(builder::initialSegmentCount);
            configOfOneProcessor.maxClaimedSegments()
                    .filter(segments -> segments > 0)
                    .ifPresent(builder::maxClaimedSegments);
            configOfOneProcessor.workerThreadPoolSize()
                    .filter(size -> size > 0)
                    .ifPresent(size -> builder
                            .workerExecutor(Executors.newScheduledThreadPool(size, new AxonThreadFactory("Worker - " + name))));
            builder.coordinatorExecutor(Executors.newScheduledThreadPool(1, new AxonThreadFactory("Coordinator - " + name)));
            if (configOfOneProcessor.enabledCoordinatorClaimExtension()) {
                builder.enableCoordinatorClaimExtension();
            }
            if (name != null) {
                builder.name(name);
            }
            return builder;
        };
    }

    private TrackingToken initialToken(StreamableMessageSource<TrackedEventMessage<?>> messageSource,
            PooledProcessorConf.ConfigOfOneProcessor processorConfig) {
        return TokenBuilder.with(messageSource)
                .atPosition(processorConfig.initialPosition())
                .build();
    }

}
