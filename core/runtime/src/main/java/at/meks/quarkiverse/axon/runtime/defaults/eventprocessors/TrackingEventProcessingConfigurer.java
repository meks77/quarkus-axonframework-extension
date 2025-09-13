package at.meks.quarkiverse.axon.runtime.defaults.eventprocessors;

import static at.meks.validation.args.ArgValidator.validate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.meks.quarkiverse.axon.runtime.conf.TrackingProcessorConf;
import at.meks.quarkiverse.axon.runtime.conf.TrackingProcessorConf.ConfigOfOneProcessor;
import at.meks.quarkiverse.axon.runtime.customizations.AxonEventProcessingConfigurer;

@ApplicationScoped
public class TrackingEventProcessingConfigurer implements AxonEventProcessingConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(TrackingEventProcessingConfigurer.class);

    @Inject
    TrackingProcessorConf trackingProcessorConf;

    @Override
    public void configure(EventProcessingConfigurer configurer) {
        ConfigOfOneProcessor defaultConfiguration = registerDefaultConfiguration(configurer);
        registerConfiguredTrackingEventProcessors(configurer, defaultConfiguration);
        assignProcessingGroupsToEventProcessors(configurer);
    }

    private ConfigOfOneProcessor registerDefaultConfiguration(EventProcessingConfigurer configurer) {
        ConfigOfOneProcessor defaultConfiguration = trackingProcessorConf.eventprocessorConfigs().get("default");
        if (defaultConfiguration.processingGroupNames().isPresent()) {
            LOG.warn(
                    "processing groups names are not supported for default event processor! The configured groups '{}' are not considered",
                    defaultConfiguration.processingGroupNames().get());
        }
        TrackingEventProcessorConfiguration tepConfig = createTrackingProcessorConfiguration(
                defaultConfiguration, defaultConfiguration);
        configurer.registerTrackingEventProcessorConfiguration(conf -> tepConfig);
        return defaultConfiguration;
    }

    private void registerConfiguredTrackingEventProcessors(EventProcessingConfigurer configurer,
            ConfigOfOneProcessor defaultConfiguration) {
        trackingProcessorConf.eventprocessorConfigs().entrySet().stream()
                .filter(entry -> !entry.getKey().equals("default"))
                .map(entry -> Map.entry(entry.getKey(),
                        createTrackingProcessorConfiguration(entry.getValue(), defaultConfiguration)))
                .forEach(entry -> addProcessorConfig(configurer, entry.getKey(), entry.getValue()));
    }

    private void assignProcessingGroupsToEventProcessors(EventProcessingConfigurer configurer) {
        trackingProcessorConf.eventprocessorConfigs().entrySet().stream()
                .filter(entry -> !entry.getKey().equals("default"))
                .forEach(entry -> entry.getValue().processingGroupNames()
                        .ifPresentOrElse(
                                groupNames -> groupNames.stream()
                                        .map(String::trim)
                                        .forEach(groupName -> configurer.assignProcessingGroup(groupName,
                                                entry.getKey())),
                                () -> LOG.warn(
                                        "processing group names not configured for the processor {}",
                                        entry.getKey())));
    }

    private static TrackingEventProcessorConfiguration createTrackingProcessorConfiguration(
            ConfigOfOneProcessor configOfOneProcessor, ConfigOfOneProcessor defaultConfiguration) {
        int threadCount = configOfOneProcessor.threadCount().or(defaultConfiguration::threadCount).orElse(1);
        validate().that(threadCount).isGreater(0);
        var trackingEventProcessorConfiguration = TrackingEventProcessorConfiguration
                .forParallelProcessing(threadCount);
        configOfOneProcessor.initialPosition()
                .or(defaultConfiguration::initialPosition)
                .ifPresent(initialPosition -> trackingEventProcessorConfiguration.andInitialTrackingToken(
                        messageSource -> TokenBuilder.with(messageSource).atPosition(initialPosition).build()));

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

}
