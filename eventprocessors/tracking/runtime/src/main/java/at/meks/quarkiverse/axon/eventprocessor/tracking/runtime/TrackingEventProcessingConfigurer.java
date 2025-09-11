package at.meks.quarkiverse.axon.eventprocessor.tracking.runtime;

import static at.meks.validation.args.ArgValidator.validate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;

import at.meks.quarkiverse.axon.eventprocessor.tracking.runtime.TrackingProcessorConf.ConfigOfOneProcessor;
import at.meks.quarkiverse.axon.eventprocessors.shared.TokenBuilder;
import at.meks.quarkiverse.axon.runtime.customizations.AxonEventProcessingConfigurer;

@ApplicationScoped
public class TrackingEventProcessingConfigurer implements AxonEventProcessingConfigurer {

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
        trackingProcessorConf.eventprocessorConfigs()
                .forEach((key, value) -> value.processingGroupNames().ifPresent(
                        processingGroupNames -> processingGroupNames
                                .stream().map(String::trim)
                                .forEach(groupName -> configurer.assignProcessingGroup(groupName, key))));
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
