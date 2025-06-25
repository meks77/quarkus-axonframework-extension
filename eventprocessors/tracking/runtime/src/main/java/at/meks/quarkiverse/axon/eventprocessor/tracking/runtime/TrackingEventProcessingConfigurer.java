package at.meks.quarkiverse.axon.eventprocessor.tracking.runtime;

import static at.meks.validation.args.ArgValidator.validate;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;

import at.meks.quarkiverse.axon.eventprocessors.shared.TokenBuilder;
import at.meks.quarkiverse.axon.runtime.customizations.AxonEventProcessingConfigurer;

@ApplicationScoped
public class TrackingEventProcessingConfigurer implements AxonEventProcessingConfigurer {

    @Inject
    TrackingProcessorConf trackingProcessorConf;

    @Override
    public void configure(EventProcessingConfigurer configurer, Collection<Object> eventhandlers) {
        configurer.usingTrackingEventProcessors();
        trackingProcessorConf.eventprocessorConfigs().entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), createTrackingProcessorConfiguration(entry.getValue())))
                .forEach(entry -> addProcessorConfig(configurer, entry.getKey(), entry.getValue()));
    }

    private static TrackingEventProcessorConfiguration createTrackingProcessorConfiguration(
            TrackingProcessorConf.ConfigOfOneProcessor configOfOneProcessor) {
        int threadCount = configOfOneProcessor.threadCount();
        validate().that(threadCount).isGreater(0);
        var trackingEventProcessorConfiguration = TrackingEventProcessorConfiguration
                .forParallelProcessing(threadCount);

        trackingEventProcessorConfiguration
                .andInitialTrackingToken(
                        messageSource -> TokenBuilder.with(messageSource)
                                .atPosition(configOfOneProcessor.initialPosition())
                                .build());

        configOfOneProcessor.batchSize()
                .filter(size -> size > 1)
                .ifPresent(trackingEventProcessorConfiguration::andBatchSize);

        configOfOneProcessor.initialSegments()
                .filter(segments -> segments >= 1)
                .ifPresent(trackingEventProcessorConfiguration::andInitialSegmentsCount);

        Optional.of(configOfOneProcessor.tokenClaim().interval())
                .filter(interval -> interval > 0)
                .ifPresent(interval -> trackingEventProcessorConfiguration.andTokenClaimInterval(interval,
                        configOfOneProcessor.tokenClaim().timeUnit()));
        return trackingEventProcessorConfiguration;
    }

    private void addProcessorConfig(EventProcessingConfigurer configurer, String groupName,
            TrackingEventProcessorConfiguration trackingEventProcessorConfiguration) {
        if (groupName.equals("default")) {
            configurer.registerTrackingEventProcessorConfiguration(
                    conf -> trackingEventProcessorConfiguration);
        } else {
            configurer.registerTrackingEventProcessorConfiguration(groupName,
                    configuration -> trackingEventProcessorConfiguration);
        }
    }

}
