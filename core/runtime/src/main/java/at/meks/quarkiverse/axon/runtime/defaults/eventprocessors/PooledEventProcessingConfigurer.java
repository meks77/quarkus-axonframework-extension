package at.meks.quarkiverse.axon.runtime.defaults.eventprocessors;

import static java.util.concurrent.Executors.newScheduledThreadPool;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.common.AxonThreadFactory;
import org.axonframework.common.configuration.Configuration;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.eventhandling.configuration.EventHandlingComponentsConfigurer;
import org.axonframework.messaging.eventhandling.processing.streaming.pooled.PooledStreamingEventProcessorConfiguration;
import org.axonframework.messaging.eventhandling.processing.streaming.pooled.PooledStreamingEventProcessorsConfigurer;
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.TokenStore;
import org.axonframework.messaging.eventstreaming.StreamableEventSource;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.meks.quarkiverse.axon.runtime.conf.PooledProcessorConf;
import at.meks.quarkiverse.axon.runtime.conf.PooledProcessorConf.ConfigOfOneProcessor;

@ApplicationScoped
public class PooledEventProcessingConfigurer extends AbstractEventProcessingConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(PooledEventProcessingConfigurer.class);

    @Inject
    PooledProcessorConf pooledProcessorConf;

    public void configure(EventSourcingConfigurer configurer, Set<Object> eventhandlers) {
        configurer.messaging(messagingConfigurer -> messagingConfigurer.eventProcessing(
                eventProcessingConfigurer -> eventProcessingConfigurer.pooledStreaming(
                        pooledStreamingEventProcessorsConfigurer -> configurePooledStreamingProcessor(
                                pooledStreamingEventProcessorsConfigurer, eventhandlers))));
    }

    private PooledStreamingEventProcessorsConfigurer configurePooledStreamingProcessor(
            PooledStreamingEventProcessorsConfigurer pooledStreamingEventProcessorsConfigurer, Set<Object> eventhandlers) {
        ConfigOfOneProcessor defaultConfig = pooledProcessorConf.eventprocessorConfigs().get("default");
        pooledStreamingEventProcessorsConfigurer.defaults(
                (configuration, pooledStreamingEventProcessorConfiguration) -> configureEventProcessor(
                        pooledStreamingEventProcessorConfiguration, configuration,
                        defaultConfig, defaultConfig));
        for (Map.Entry<String, ConfigOfOneProcessor> entry : nonDefaultProcessorConfigurations()) {
            LOG.info("registering pooled event processor with name {}", entry.getKey());
            ConfigOfOneProcessor configOfOneProcessor = entry.getValue();
            String processorName = createProcessorName(entry.getKey(), configOfOneProcessor.useRandomUuidSuffix()
                    .or(defaultConfig::useRandomUuidSuffix)
                    .orElse(false));

            pooledStreamingEventProcessorsConfigurer.processor(processorName,
                    config -> config.eventHandlingComponents(
                            requiredComponentPhase -> configureHandlingComponents(requiredComponentPhase,
                                    eventhandlers))
                            .customized(
                                    (configuration, pooledStreamingEventProcessorConfiguration) -> configureEventProcessor(
                                            pooledStreamingEventProcessorConfiguration, configuration,
                                            entry.getValue(), defaultConfig)));
        }
        return pooledStreamingEventProcessorsConfigurer;
    }

    private EventHandlingComponentsConfigurer.CompletePhase configureHandlingComponents(
            EventHandlingComponentsConfigurer.RequiredComponentPhase requiredComponentPhase, Set<Object> eventhandlers) {
        //        todo there be magic
        EventHandlingComponentsConfigurer.AdditionalComponentPhase x = null;
        for (var eventhandler : eventhandlers) {
            x = requiredComponentPhase.autodetected(config -> eventhandler);
        }

        //        configOfOneProcessor.processingGroupNames()
        //                .ifPresentOrElse(
        //                        groupNames -> assignProcessingGroupsToProcessor(configurer, groupNames, processorName),
        //                        () -> LOG.warn(
        //                                "processing group names not configured for the processor {}",
        //                                processorName));

        return x;
    }

    private Set<Map.Entry<String, ConfigOfOneProcessor>> nonDefaultProcessorConfigurations() {
        return pooledProcessorConf.eventprocessorConfigs()
                .entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equals("default"))
                .collect(Collectors.toSet());
    }

    private static @NonNull PooledStreamingEventProcessorConfiguration configureEventProcessor(
            PooledStreamingEventProcessorConfiguration pooledStreamingEventProcessorConfiguration, Configuration configuration,
            ConfigOfOneProcessor namedConfig, ConfigOfOneProcessor defaultConfig) {
        getInitialPositionConfig(namedConfig, defaultConfig)
                .ifPresent(
                        posConfig -> pooledStreamingEventProcessorConfiguration.initialToken(
                                trackingTokenSource -> TokenBuilder.with(
                                        pooledStreamingEventProcessorConfiguration.processorName(),
                                        trackingTokenSource).and(posConfig)));
        pooledStreamingEventProcessorConfiguration.eventSource(
                configuration.getComponent(
                        StreamableEventSource.class));
        namedConfig.batchSize().or(defaultConfig::batchSize).filter(batchSize -> batchSize > 0).ifPresent(
                pooledStreamingEventProcessorConfiguration::batchSize);
        namedConfig.initialSegments().or(defaultConfig::initialSegments).filter(
                initialSegments -> initialSegments > 0).ifPresent(
                        pooledStreamingEventProcessorConfiguration::initialSegmentCount);
        namedConfig.maxClaimedSegments().or(defaultConfig::maxClaimedSegments).filter(
                maxClaimedSegments -> maxClaimedSegments > 0).ifPresent(
                        pooledStreamingEventProcessorConfiguration::maxClaimedSegments);
        if (namedConfig.enabledCoordinatorClaimExtension().or(
                defaultConfig::enabledCoordinatorClaimExtension).orElse(false)) {
            pooledStreamingEventProcessorConfiguration.enableCoordinatorClaimExtension();
        }
        if (shouldUseInMemoryTokenStore(namedConfig, defaultConfig)) {
            pooledStreamingEventProcessorConfiguration.tokenStore(getSingletonInMemoryTokenStore());
        }
        pooledStreamingEventProcessorConfiguration.tokenStore(configuration.getComponent(TokenStore.class));
        namedConfig.workerThreadPoolSize().or(defaultConfig::workerThreadPoolSize).filter(
                poolSize -> poolSize > 0).ifPresent(
                        size -> pooledStreamingEventProcessorConfiguration.workerExecutor(
                                newScheduledThreadPool(size,
                                        new AxonThreadFactory(
                                                "Worker - " + pooledStreamingEventProcessorConfiguration.processorName()))));
        // TODO configure coordinatorExecutor?
        return pooledStreamingEventProcessorConfiguration;
    }

    private static boolean shouldUseInMemoryTokenStore(ConfigOfOneProcessor configOfOneProcessor,
            ConfigOfOneProcessor defaultConfig) {
        return configOfOneProcessor.useInMemoryTokenStore().or(
                defaultConfig::useInMemoryTokenStore).orElse(false);
    }

}
