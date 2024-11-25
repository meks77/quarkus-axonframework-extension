package at.meks.quarkiverse.axon.eventprocessor.tracking.runtime;

import static at.meks.validation.args.ArgValidator.validate;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;

import at.meks.quarkiverse.axon.eventprocessors.shared.TokenBuilder;
import at.meks.quarkiverse.axon.runtime.AxonEventProcessingConfigurer;

@ApplicationScoped
public class TrackingEventProcessingConfigurer implements AxonEventProcessingConfigurer {

    @Inject
    TrackingProcessorConf trackingProcessorConf;

    @Override
    public void configure(EventProcessingConfigurer configurer) {
        int threadCount = trackingProcessorConf.threadCount();
        validate().that(threadCount).isGreater(0);
        var trackingEventProcessorConfiguration = TrackingEventProcessorConfiguration
                .forParallelProcessing(threadCount);

        trackingEventProcessorConfiguration
                .andInitialTrackingToken(
                        messageSource -> TokenBuilder.with(messageSource)
                                .atPosition(trackingProcessorConf.initialPosition())
                                .build());

        Optional.of(trackingProcessorConf.batchSize())
                .filter(size -> size > 1)
                .ifPresent(trackingEventProcessorConfiguration::andBatchSize);

        Optional.of(trackingProcessorConf.initialSegments())
                .filter(segments -> segments >= 1)
                .ifPresent(trackingEventProcessorConfiguration::andInitialSegmentsCount);

        Optional.of(trackingProcessorConf.tokenClaim().interval())
                .filter(interval -> interval > 0)
                .ifPresent(interval -> trackingEventProcessorConfiguration.andTokenClaimInterval(interval,
                        trackingProcessorConf.tokenClaim().timeUnit()));

        configurer.usingTrackingEventProcessors();
        configurer.registerTrackingEventProcessorConfiguration(
                conf -> trackingEventProcessorConfiguration);
    }

}
