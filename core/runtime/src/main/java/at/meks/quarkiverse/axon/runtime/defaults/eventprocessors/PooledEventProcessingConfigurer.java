package at.meks.quarkiverse.axon.runtime.defaults.eventprocessors;

import static java.util.concurrent.Executors.newScheduledThreadPool;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Override
    public void configure(EventSourcingConfigurer configurer,
            Stream<EventhandlersPerNamespace.EventhandlersOfANamespace> eventhandlers) {
        configurer.messaging(messagingConfigurer -> messagingConfigurer.eventProcessing(
                eventProcessingConfigurer -> eventProcessingConfigurer.pooledStreaming(
                        pooledStreamingEventProcessorsConfigurer -> configurePooledStreamingProcessor(
                                pooledStreamingEventProcessorsConfigurer, eventhandlers))));
    }

    private PooledStreamingEventProcessorsConfigurer configurePooledStreamingProcessor(
            PooledStreamingEventProcessorsConfigurer pooledStreamingEventProcessorsConfigurer,
            Stream<EventhandlersPerNamespace.EventhandlersOfANamespace> eventhandlers) {
        ConfigOfOneProcessor defaultConfig = pooledProcessorConf.eventprocessorConfigs().get("default");
        pooledStreamingEventProcessorsConfigurer.defaults(
                (configuration, pooledStreamingEventProcessorConfiguration) -> configureEventProcessor(
                        pooledStreamingEventProcessorConfiguration, configuration,
                        defaultConfig, defaultConfig));
        // TODO: group eventhandlers by namespace at compile time!!!
        // TODO: Warning for configured, but unused event processors
        // TODO: exclude eventhandlers with namespaces, configured for subscribing processors
        // TODO: throw exception if more processors are responsible for one namespace
        // TODO: rename group to namespace
        eventhandlers
                .forEach(namespace -> {
                    LOG.info("registering pooled event processor for namespaces {}", namespace.namespaceName().value());
                    Map.Entry<String, ConfigOfOneProcessor> processorNameAndConfig = processorForNamespace(namespace)
                            .orElseGet(() -> processorWithName(namespace)
                                    .orElseGet(() -> Map.entry(namespace.namespaceName().value(), defaultConfig)));
                    ConfigOfOneProcessor processorConfig = processorNameAndConfig.getValue();
                    String processorName = createProcessorName(processorNameAndConfig.getKey(),
                            processorConfig.useRandomUuidSuffix().or(defaultConfig::useRandomUuidSuffix).orElse(false));

                    pooledStreamingEventProcessorsConfigurer.processor(processorName,
                            config -> config.eventHandlingComponents(
                                    requiredComponentPhase -> configureHandlingComponents(requiredComponentPhase,
                                            namespace.eventhandlers()))
                                    .customized(
                                            (configuration,
                                                    pooledStreamingEventProcessorConfiguration) -> configureEventProcessor(
                                                            pooledStreamingEventProcessorConfiguration, configuration,
                                                            processorConfig, defaultConfig)));
                });
        return pooledStreamingEventProcessorsConfigurer;
    }

    private @NonNull Optional<Map.Entry<String, ConfigOfOneProcessor>> processorWithName(
            EventhandlersPerNamespace.EventhandlersOfANamespace namespace) {
        return Optional.ofNullable(
                pooledProcessorConf.eventprocessorConfigs().get(namespace.namespaceName().value())).map(
                        config -> Map.entry(namespace.namespaceName().value(), config));
    }

    private @NonNull Optional<Map.Entry<String, ConfigOfOneProcessor>> processorForNamespace(
            EventhandlersPerNamespace.EventhandlersOfANamespace namespace) {
        List<Map.Entry<String, ConfigOfOneProcessor>> processorConfigs = nonDefaultProcessorConfigurations()
                .stream()
                .filter(entry -> entry.getValue().processingGroupNames()
                        .map(groupNames -> groupNames.contains(namespace.namespaceName().value()))
                        .orElse(false))
                .toList();
        if (processorConfigs.size() > 1) {
            throw new IllegalStateException(
                    "Multiple processors for namespace " + namespace.namespaceName().value() + " found");
        }
        return processorConfigs.stream().findFirst();
    }

    private EventHandlingComponentsConfigurer.CompletePhase configureHandlingComponents(
            EventHandlingComponentsConfigurer.RequiredComponentPhase requiredComponentPhase,
            Collection<EventhandlersPerNamespace.Eventhandler> eventhandlers) {
        EventHandlingComponentsConfigurer.AdditionalComponentPhase componentPhase = null;
        for (var eventhandler : eventhandlers) {
            componentPhase = requiredComponentPhase.autodetected(eventhandler.name(), config -> eventhandler.instance());
        }

        return componentPhase;
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
                .ifPresent(posConfig -> pooledStreamingEventProcessorConfiguration.initialToken(
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
        } else {
            pooledStreamingEventProcessorConfiguration.tokenStore(configuration.getComponent(TokenStore.class));
        }
        namedConfig.workerThreadPoolSize()
                .or(defaultConfig::workerThreadPoolSize)
                .filter(poolSize -> poolSize > 0)
                .ifPresent(size -> pooledStreamingEventProcessorConfiguration.workerExecutor(
                        newScheduledThreadPool(size, new AxonThreadFactory(
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
