package at.meks.quarkiverse.axon.eventprocessor.pooled.runtime;

import static java.util.concurrent.Executors.newScheduledThreadPool;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.common.AxonThreadFactory;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.messaging.StreamableMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.meks.quarkiverse.axon.eventprocessors.shared.TokenBuilder;
import at.meks.quarkiverse.axon.runtime.customizations.AxonEventProcessingConfigurer;

@ApplicationScoped
public class PooledEventProcessingConfigurer implements AxonEventProcessingConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(PooledEventProcessingConfigurer.class);

    @Inject
    PooledProcessorConf pooledProcessorConf;

    @Override
    public void configure(EventProcessingConfigurer configurer) {

        for (Map.Entry<String, PooledProcessorConf.ConfigOfOneProcessor> e : pooledProcessorConf.eventprocessorConfigs()
                .entrySet()) {
            LOG.info("registering pooled event processor with name {}", e.getKey());
            configurer.registerPooledStreamingEventProcessor(e.getKey(), Configuration::eventStore,
                    createProcessorConfig(e.getValue(), e.getKey()));
            Optional<List<String>> groupNames = e.getValue().processingGroupNames();
            if (groupNames.isPresent()) {
                for (String groupName : groupNames.get()) {
                    LOG.info("assigning processing group {} to event processor {}", groupName, e.getKey());
                    configurer.assignProcessingGroup(groupName.trim(), e.getKey());
                }
            }
        }
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
                            .workerExecutor(newScheduledThreadPool(size, new AxonThreadFactory("Worker - " + name))));
            builder.coordinatorExecutor(newScheduledThreadPool(1, new AxonThreadFactory("Coordinator - " + name)));
            if (configOfOneProcessor.enabledCoordinatorClaimExtension()) {
                builder.enableCoordinatorClaimExtension();
            }
            builder.name(name);
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
