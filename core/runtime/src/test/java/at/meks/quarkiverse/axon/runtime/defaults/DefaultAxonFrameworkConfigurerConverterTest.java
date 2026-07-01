package at.meks.quarkiverse.axon.runtime.defaults;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import jakarta.enterprise.inject.Instance;

import org.axonframework.common.configuration.AxonConfiguration;
import org.axonframework.conversion.Converter;
import org.axonframework.conversion.DelegatingGeneralConverter;
import org.axonframework.conversion.GeneralConverter;
import org.axonframework.messaging.core.conversion.DelegatingMessageConverter;
import org.axonframework.messaging.core.conversion.MessageConverter;
import org.axonframework.messaging.core.unitofwork.transaction.TransactionManager;
import org.axonframework.messaging.eventhandling.conversion.DelegatingEventConverter;
import org.axonframework.messaging.eventhandling.conversion.EventConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.meks.quarkiverse.axon.runtime.conf.SubscribingProcessorConf;
import at.meks.quarkiverse.axon.runtime.customizations.AxonConverterProducer;
import at.meks.quarkiverse.axon.runtime.customizations.AxonMetricsConfigurer;
import at.meks.quarkiverse.axon.runtime.customizations.EventstoreConfigurer;
import at.meks.quarkiverse.axon.runtime.customizations.TokenStoreConfigurer;

class DefaultAxonFrameworkConfigurerConverterTest {

    private final Converter generalConverter = mock(Converter.class);
    private final Converter eventConverter = mock(Converter.class);
    private final Converter messageConverter = mock(Converter.class);

    private final AxonConverterProducer axonConverterProducer = new AxonConverterProducer() {
        @Override
        public Converter createGeneralConverter() {
            return generalConverter;
        }

        @Override
        public Converter createEventConverter() {
            return eventConverter;
        }

        @Override
        public Converter createMessageConverter() {
            return messageConverter;
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
        configurer.interceptorConfigurer = mock(InterceptorConfigurer.class);
        configurer.axonConverterProducer = axonConverterProducer;
        configurer.retrySchedulerProducer = mock(Instance.class);
        configurer.commandBusConfigurer = mock(CommandBusConfigurer.class);
        configurer.axonTracingConfigurer = mock(Instance.class);
        configurer.axonConfiguration = mock(at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration.class);
        configurer.axonComponentSetup = mock(AxonComponentenSetup.class);

        when(configurer.axonTracingConfigurer.isResolvable()).thenReturn(false);

        SubscribingProcessorConf subscribingProcessorConf = mock(SubscribingProcessorConf.class);
        when(configurer.axonConfiguration.subscribingProcessorConf()).thenReturn(subscribingProcessorConf);
        when(subscribingProcessorConf.namespaces()).thenReturn(Optional.empty());

        configurer.eventSourcedEntityClasses(emptySet());
        configurer.commandhandlers(emptySet());
        configurer.queryhandlers(emptySet());
        configurer.eventhandlers(emptySet());
    }

    @Test
    void providedAxonConverterProducerConfiguresAllAxonConverters() {
        AxonConfiguration configuration = configurer.configure().build();

        DelegatingGeneralConverter registeredGeneralConverter = (DelegatingGeneralConverter) configuration
                .getComponent(GeneralConverter.class);
        DelegatingMessageConverter registeredMessageConverter = (DelegatingMessageConverter) configuration
                .getComponent(MessageConverter.class);
        DelegatingEventConverter registeredEventConverter = (DelegatingEventConverter) configuration
                .getComponent(EventConverter.class);

        assertThat(registeredGeneralConverter.delegate()).isSameAs(generalConverter);
        assertThat(registeredMessageConverter.delegate()).isSameAs(messageConverter);
        assertThat(((DelegatingMessageConverter) registeredEventConverter.delegate()).delegate()).isSameAs(eventConverter);
    }

}
