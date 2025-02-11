package at.meks.quarkiverse.axon.eventprocessor.pooled.runtime;

import java.util.Collection;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
        EventProcessingConfigurer.PooledStreamingProcessorConfiguration psepConfig = (config, builder) -> {
            builder.initialToken(this::initialToken);
            builder.tokenStore(config.getComponent(TokenStore.class));
            Optional.of(pooledProcessorConf.batchSize())
                    .filter(size -> size > 0)
                    .ifPresent(builder::batchSize);
            Optional.of(pooledProcessorConf.initialSegments())
                    .filter(segments -> segments > 0)
                    .ifPresent(builder::initialSegmentCount);
            Optional.of(pooledProcessorConf.maxClaimedSegments())
                    .filter(segments -> segments > 0)
                    .ifPresent(builder::maxClaimedSegments);
            if (pooledProcessorConf.enabledCoordinatorClaimExtension()) {
                builder.enableCoordinatorClaimExtension();
            }
            return builder;
        };

        configurer.usingPooledStreamingEventProcessors();
        configurer.registerPooledStreamingEventProcessorConfiguration(psepConfig);
    }

    private TrackingToken initialToken(StreamableMessageSource<TrackedEventMessage<?>> messageSource) {
        return TokenBuilder.with(messageSource)
                .atPosition(pooledProcessorConf.initialPosition())
                .build();
    }

}
