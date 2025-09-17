package at.meks.quarkiverse.axon.runtime.defaults.eventprocessors;

import static at.meks.validation.args.ArgValidator.validate;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.meks.quarkiverse.axon.runtime.conf.TrackingProcessorConf;
import at.meks.quarkiverse.axon.runtime.conf.TrackingProcessorConf.ConfigOfOneProcessor;

@ApplicationScoped
public class TrackingEventProcessingConfigurer extends AbstractEventProcessingConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(TrackingEventProcessingConfigurer.class);

    @Inject
    TrackingProcessorConf trackingProcessorConf;

    @Override
    public void configure(EventProcessingConfigurer configurer) {
        ConfigOfOneProcessor defaultConfiguration = registerDefaultConfiguration(configurer);
        RegisteredProcessorNames registeredProcessorNames = registerConfiguredTrackingEventProcessors(configurer,
                defaultConfiguration);
        assignProcessingGroupsToEventProcessors(configurer, registeredProcessorNames);
        assignInMemoryTokenStoreIfNecessary(configurer, registeredProcessorNames, defaultConfiguration);
    }

    private ConfigOfOneProcessor registerDefaultConfiguration(EventProcessingConfigurer configurer) {
        ConfigOfOneProcessor defaultConfiguration = trackingProcessorConf.eventprocessorConfigs().get("default");
        if (defaultConfiguration.processingGroupNames().isPresent()) {
            LOG.warn(
                    "processing groups names are not supported for default event processor! The configured groups '{}' are not considered",
                    defaultConfiguration.processingGroupNames().get());
        }
        TrackingEventProcessorConfiguration tepConfig = createTrackingProcessorConfiguration("default",
                defaultConfiguration, defaultConfiguration);
        configurer.registerTrackingEventProcessorConfiguration(conf -> tepConfig);
        return defaultConfiguration;
    }

    private RegisteredProcessorNames registerConfiguredTrackingEventProcessors(EventProcessingConfigurer configurer,
            ConfigOfOneProcessor defaultConfiguration) {
        Map<String, ConfigOfOneProcessor> processorConfigs = trackingProcessorConf.eventprocessorConfigs().entrySet().stream()
                .filter(entry -> !entry.getKey().equals("default"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, String> processorNameMap = processorConfigs.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(),
                        createProcessorName(entry.getKey(), entry.getValue().useRandomUuidSuffix().or(
                                defaultConfiguration::useRandomUuidSuffix).orElse(false))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        processorConfigs.entrySet().stream()
                .map(entry -> Map.entry(processorNameMap.get(entry.getKey()),
                        createTrackingProcessorConfiguration(entry.getKey(), entry.getValue(), defaultConfiguration)))
                .forEach(entry -> addProcessorConfig(configurer,
                        entry.getKey(),
                        entry.getValue()));
        return new RegisteredProcessorNames(processorNameMap);
    }

    private static TrackingEventProcessorConfiguration createTrackingProcessorConfiguration(String processorName,
            ConfigOfOneProcessor configOfOneProcessor, ConfigOfOneProcessor defaultConfiguration) {
        int threadCount = configOfOneProcessor.threadCount().or(defaultConfiguration::threadCount).orElse(1);
        validate().that(threadCount).isGreater(0);
        var trackingEventProcessorConfiguration = TrackingEventProcessorConfiguration
                .forParallelProcessing(threadCount);
        getInitialPositionConfig(configOfOneProcessor, defaultConfiguration)
                .ifPresent(initialPosition -> trackingEventProcessorConfiguration.andInitialTrackingToken(
                        messageSource -> TokenBuilder.with(processorName, messageSource).and(initialPosition)));

        configOfOneProcessor.batchSize().or(defaultConfiguration::batchSize)
                .filter(size -> size > 1)
                .ifPresent(trackingEventProcessorConfiguration::andBatchSize);

        configOfOneProcessor.initialSegments().or(defaultConfiguration::initialSegments)
                .filter(segments -> segments >= 1)
                .ifPresent(trackingEventProcessorConfiguration::andInitialSegmentsCount);

        configOfOneProcessor.tokenClaim().interval().or(() -> defaultConfiguration.tokenClaim().interval())
                .ifPresent(interval -> trackingEventProcessorConfiguration.andTokenClaimInterval(
                        interval,
                        configOfOneProcessor.tokenClaim().timeUnit()
                                .or(() -> defaultConfiguration.tokenClaim().timeUnit())
                                .orElse(TimeUnit.SECONDS)));
        return trackingEventProcessorConfiguration;
    }

    private void addProcessorConfig(EventProcessingConfigurer configurer, String processorName,
            TrackingEventProcessorConfiguration trackingEventProcessorConfiguration) {
        configurer.registerTrackingEventProcessor(processorName, Configuration::eventStore,
                conf -> trackingEventProcessorConfiguration);

    }

    private void assignProcessingGroupsToEventProcessors(EventProcessingConfigurer configurer,
            RegisteredProcessorNames registeredProcessorNames) {
        trackingProcessorConf.eventprocessorConfigs().entrySet().stream()
                .filter(entry -> !entry.getKey().equals("default"))
                .forEach(entry -> entry.getValue().processingGroupNames()
                        .ifPresentOrElse(
                                groupNames -> assignProcessingGroupsToProcessor(configurer, groupNames,
                                        registeredProcessorNames.getRegisteredNameFor(entry.getKey())),
                                () -> LOG.warn(
                                        "processing group names not configured for the processor {}",
                                        entry.getKey())));
    }

    private void assignInMemoryTokenStoreIfNecessary(EventProcessingConfigurer configurer,
            RegisteredProcessorNames registeredProcessorNames, ConfigOfOneProcessor defaultConfiguration) {
        trackingProcessorConf.eventprocessorConfigs().entrySet().stream()
                .filter(entry -> !entry.getKey().equals("default"))
                .forEach(entry -> {
                    boolean useInMemoryTokenStore = entry.getValue().useInMemoryTokenStore()
                            .or(defaultConfiguration::useInMemoryTokenStore)
                            .orElse(false);
                    if (useInMemoryTokenStore) {
                        configurer.registerTokenStore(registeredProcessorNames.getRegisteredNameFor(entry.getKey()),
                                config -> getSingletonInMemoryTokenStore());
                    }
                });

    }

    private static class RegisteredProcessorNames {
        private final Map<String, String> processorNameMap;

        RegisteredProcessorNames(Map<String, String> processorNameMap) {
            this.processorNameMap = processorNameMap;
        }

        String getRegisteredNameFor(String configuredName) {
            return processorNameMap.get(configuredName);
        }
    }
}
