package at.meks.quarkiverse.axon.runtime.defaults.eventprocessors;

import static java.util.concurrent.Executors.newScheduledThreadPool;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.common.AxonThreadFactory;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.tokenstore.TokenStore;
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
    public void configure(EventProcessingConfigurer configurer) {
        ConfigOfOneProcessor defaultConfig = registerDefaultConfiguration(configurer);
        registerConfiguredNamedConfigurations(configurer, defaultConfig);
    }

    private ConfigOfOneProcessor registerDefaultConfiguration(EventProcessingConfigurer configurer) {
        ConfigOfOneProcessor defaultConfig = pooledProcessorConf.eventprocessorConfigs().get("default");
        if (defaultConfig.processingGroupNames().isPresent()) {
            LOG.warn(
                    "processing groups names are not supported for default event processor! The configured groups '{}' are not considered",
                    defaultConfig.processingGroupNames().get());
        }
        configurer
                .registerPooledStreamingEventProcessorConfiguration(
                        createProcessorConfig(defaultConfig, null, defaultConfig));
        return defaultConfig;
    }

    private void registerConfiguredNamedConfigurations(EventProcessingConfigurer configurer,
            ConfigOfOneProcessor defaultConfig) {
        for (Map.Entry<String, ConfigOfOneProcessor> entry : nonDefaultProcessorConfigurations()) {
            LOG.info("registering pooled event processor with name {}", entry.getKey());
            configurer.registerPooledStreamingEventProcessor(entry.getKey(), Configuration::eventStore,
                    createProcessorConfig(entry.getValue(), entry.getKey(), defaultConfig));
            entry.getValue().processingGroupNames()
                    .ifPresentOrElse(
                            groupNames -> assignProcessingGroupsToProcessor(configurer, groupNames, entry.getKey()),
                            () -> LOG.warn(
                                    "processing group names not configured for the processor {}",
                                    entry.getKey()));
        }
    }

    private Set<Map.Entry<String, ConfigOfOneProcessor>> nonDefaultProcessorConfigurations() {
        return pooledProcessorConf.eventprocessorConfigs()
                .entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equals("default"))
                .collect(Collectors.toSet());
    }

    private EventProcessingConfigurer.PooledStreamingProcessorConfiguration createProcessorConfig(
            ConfigOfOneProcessor configOfOneProcessor, String name, ConfigOfOneProcessor defaultConfig) {
        return (config, builder) -> {
            configOfOneProcessor.initialPosition()
                    .or(defaultConfig::initialPosition)
                    .ifPresent(initialPosition -> builder.initialToken(messageSource -> TokenBuilder.with(messageSource)
                            .atPosition(initialPosition)
                            .build()));
            builder.tokenStore(config.getComponent(TokenStore.class));
            configOfOneProcessor.batchSize().or(defaultConfig::batchSize)
                    .filter(size -> size > 0)
                    .ifPresent(builder::batchSize);
            configOfOneProcessor.initialSegments().or(defaultConfig::initialSegments)
                    .filter(segments -> segments > 0)
                    .ifPresent(builder::initialSegmentCount);
            configOfOneProcessor.maxClaimedSegments().or(defaultConfig::maxClaimedSegments)
                    .filter(segments -> segments > 0)
                    .ifPresent(builder::maxClaimedSegments);
            configOfOneProcessor.workerThreadPoolSize().or(defaultConfig::workerThreadPoolSize)
                    .filter(size -> size > 0)
                    .ifPresent(size -> builder
                            .workerExecutor(newScheduledThreadPool(size, new AxonThreadFactory("Worker - " + name))));
            builder.coordinatorExecutor(newScheduledThreadPool(1, new AxonThreadFactory("Coordinator - " + name)));
            if (configOfOneProcessor.enabledCoordinatorClaimExtension().or(
                    defaultConfig::enabledCoordinatorClaimExtension)
                    .orElse(false)) {
                builder.enableCoordinatorClaimExtension();
            }
            if (name != null) {
                builder.name(name);
            }
            return builder;
        };
    }

}
