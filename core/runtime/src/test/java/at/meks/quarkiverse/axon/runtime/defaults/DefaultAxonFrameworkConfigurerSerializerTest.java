package at.meks.quarkiverse.axon.runtime.defaults;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import jakarta.enterprise.inject.Instance;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.conf.SubscribingProcessorConf;
import at.meks.quarkiverse.axon.runtime.customizations.AxonMetricsConfigurer;
import at.meks.quarkiverse.axon.runtime.customizations.AxonSerializerProducer;
import at.meks.quarkiverse.axon.runtime.customizations.EventstoreConfigurer;
import at.meks.quarkiverse.axon.runtime.customizations.TokenStoreConfigurer;

class DefaultAxonFrameworkConfigurerSerializerTest {

    private final Serializer serializer = JacksonSerializer.defaultSerializer();
    private final Serializer eventSerializer = JacksonSerializer.defaultSerializer();
    private final Serializer messageSerializer = JacksonSerializer.defaultSerializer();

    private final AxonSerializerProducer axonSerializerProducer = new AxonSerializerProducer() {
        @Override
        public Serializer createSerializer() {
            return serializer;
        }

        @Override
        public Serializer createEventSerializer() {
            return eventSerializer;
        }

        @Override
        public Serializer createMessageSerializer() {
            return messageSerializer;
        }
    };

    private DefaultAxonFrameworkConfigurer configurer;

    @BeforeEach
    void setUp() {
        configurer = new DefaultAxonFrameworkConfigurer();
        configurer.transactionManager = mock(TransactionManager.class);
        configurer.tokenStoreConfigurer = mock(TokenStoreConfigurer.class);
        configurer.metricsConfigurer = mock(AxonMetricsConfigurer.class);
        configurer.eventstoreConfigurer = mock(EventstoreConfigurer.class);
        configurer.eventProcessingConfigurers = mock(Instance.class);
        configurer.eventUpcasterChain = mock(Instance.class);
        configurer.interceptorConfigurer = mock(InterceptorConfigurer.class);
        configurer.axonSerializerProducer = axonSerializerProducer;
        configurer.retrySchedulerProducer = mock(Instance.class);
        configurer.retrySchedulerConfigurer = mock(RetrySchedulerConfigurer.class);
        configurer.commandBusConfigurer = mock(CommandBusConfigurer.class);
        configurer.axonTracingConfigurer = mock(Instance.class);
        configurer.axonConfiguration = mock(AxonConfiguration.class);
        configurer.axonComponentSetup = mock(AxonComponentenSetup.class);

        when(configurer.retrySchedulerConfigurer.retryScheduler()).thenReturn(Optional.empty());
        when(configurer.eventUpcasterChain.isResolvable()).thenReturn(false);
        when(configurer.eventUpcasterChain.isAmbiguous()).thenReturn(false);
        when(configurer.axonTracingConfigurer.isResolvable()).thenReturn(false);

        AxonConfiguration.EventProcessingConfig eventProcessingConfig = mock(AxonConfiguration.EventProcessingConfig.class);
        when(configurer.axonConfiguration.eventProcessing()).thenReturn(eventProcessingConfig);
        when(eventProcessingConfig.defaultEventProcessingType()).thenReturn(Optional.empty());
        SubscribingProcessorConf subscribingProcessorConf = mock(SubscribingProcessorConf.class);
        when(configurer.axonConfiguration.subscribingProcessorConf()).thenReturn(subscribingProcessorConf);
        when(subscribingProcessorConf.processingGroupNames()).thenReturn(Optional.empty());

        configurer.aggregateClasses(emptySet());
        configurer.commandhandlers(emptySet());
        configurer.queryhandlers(emptySet());
        configurer.eventhandlers(emptySet());
        configurer.sagaClasses(emptySet());
    }

    @Test
    void providedAxonSerializerProducerConfiguresAllAxonSerializers() {
        Configuration configuration = configurer.configure().buildConfiguration();

        assertThat(configuration.serializer()).isSameAs(serializer);
        assertThat(configuration.eventSerializer()).isSameAs(eventSerializer);
        assertThat(configuration.messageSerializer()).isSameAs(messageSerializer);
    }

}
