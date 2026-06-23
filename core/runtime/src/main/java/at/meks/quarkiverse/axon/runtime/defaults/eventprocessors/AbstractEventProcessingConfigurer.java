package at.meks.quarkiverse.axon.runtime.defaults.eventprocessors;

import java.util.Optional;

import org.axonframework.messaging.eventhandling.processing.streaming.token.store.TokenStore;
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.inmemory.InMemoryTokenStore;

import at.meks.quarkiverse.axon.runtime.conf.StreamingProcessorConf;
import at.meks.quarkiverse.axon.runtime.conf.StreamingProcessorConf.InitialPosition;
import at.meks.quarkiverse.axon.runtime.customizations.AxonEventProcessingConfigurer;

public abstract class AbstractEventProcessingConfigurer implements AxonEventProcessingConfigurer {

    private static InMemoryTokenStore singletonInMemoryTokenStore;

    protected static synchronized TokenStore getSingletonInMemoryTokenStore() {
        if (singletonInMemoryTokenStore == null) {
            singletonInMemoryTokenStore = new InMemoryTokenStore();
        }
        return singletonInMemoryTokenStore;
    }

    protected static String createProcessorName(String configuredName, boolean useUuidSuffix) {
        if (useUuidSuffix) {
            return configuredName + "-" + java.util.UUID.randomUUID();
        }
        return configuredName;
    }

    static Optional<InitialPosition> getInitialPositionConfig(StreamingProcessorConf configOfOneProcessor,
            StreamingProcessorConf defaultConfig) {
        return initialPositionIfAValueIsSet(configOfOneProcessor.initialPosition())
                .or(() -> initialPositionIfAValueIsSet(defaultConfig.initialPosition()));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static Optional<InitialPosition> initialPositionIfAValueIsSet(
            Optional<InitialPosition> initialPositionOfProcessor) {
        if (initialPositionOfProcessor.isPresent()) {
            InitialPosition conf = initialPositionOfProcessor.get();
            if (conf.atDuration().isPresent() || conf.atSequence().isPresent() || conf.atHeadOrTail().isPresent()
                    || conf.atTimestamp().isPresent()) {
                return Optional.of(conf);
            }
        }
        return Optional.empty();
    }
}
