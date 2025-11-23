package at.meks.quarkiverse.axon.runtime.defaults;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.axonframework.config.AggregateConfiguration;
import org.axonframework.config.Configurer;
import org.axonframework.config.EventProcessingConfigurer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration.ComponentDiscovery;
import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration.ComponentsDiscovery;
import at.meks.quarkiverse.axon.runtime.customizations.QuarkusAggregateConfigurer;
import at.meks.quarkiverse.axon.runtime.customizations.SagaStoreConfigurer;
import at.meks.quarkiverse.axon.shared.model.CardReturnSaga;
import at.meks.quarkiverse.axon.shared.model.DomainServiceExample;
import at.meks.quarkiverse.axon.shared.model.Giftcard;
import at.meks.quarkiverse.axon.shared.projection.GiftcardQueryHandler;

@ExtendWith(MockitoExtension.class)
class AxonComponentenSetupTest {

    @InjectMocks
    private AxonComponentenSetup setup = new AxonComponentenSetup();

    @Mock(strictness = Mock.Strictness.LENIENT)
    private Configurer configurer;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private AxonConfiguration axonConfiguration;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private ComponentsDiscovery componentsDiscovery;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private ComponentDiscovery componentDiscovery;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private QuarkusAggregateConfigurer aggregateConfigurer;

    @SuppressWarnings("unused")
    @Mock
    SagaStoreConfigurer sagaStoreConfigurer;

    @BeforeEach
    void setUp() {
        when(axonConfiguration.discovery()).thenReturn(componentsDiscovery);
    }

    @Nested
    class AggregateDiscovery {

        Set<Class<?>> classes = Set.of(Giftcard.class);

        @Mock(strictness = Mock.Strictness.LENIENT)
        AggregateConfiguration<Giftcard> aggregateConfiguration;

        @BeforeEach
        void setUp() {
            when(componentsDiscovery.aggregates()).thenReturn(componentDiscovery);
            when(aggregateConfigurer.createConfigurer(Giftcard.class)).thenReturn(aggregateConfiguration);
        }

        @Test
        void enabledAggregateDiscovery() {
            when(componentDiscovery.enabled()).thenReturn(true);
            setup.configureAggregates(configurer, classes);
            verify(configurer, Mockito.times(1)).configureAggregate(aggregateConfiguration);
        }

        @Test
        void disabledAggregateDiscovery() {
            when(componentDiscovery.enabled()).thenReturn(false);
            setup.configureAggregates(configurer, classes);
            //noinspection unchecked
            verify(configurer, Mockito.never()).configureAggregate(any(AggregateConfiguration.class));
        }
    }

    @Nested
    class CommandHandlersDiscovery {

        Set<Object> handlers = Set.of(DomainServiceExample.class);

        @BeforeEach
        void setUp() {
            when(componentsDiscovery.commandHandlers()).thenReturn(componentDiscovery);
        }

        @Test
        void enabledAggregateDiscovery() {
            when(componentDiscovery.enabled()).thenReturn(true);
            setup.configureCommandHandlers(configurer, handlers);
            verify(configurer, Mockito.times(1)).registerCommandHandler(any());
        }

        @Test
        void disabledAggregateDiscovery() {
            when(componentDiscovery.enabled()).thenReturn(false);
            setup.configureCommandHandlers(configurer, handlers);
            verify(configurer, Mockito.never()).registerCommandHandler(any());
        }
    }

    @Nested
    class EventHandlersDiscovery {

        Set<Object> handlers = Set.of(new GiftcardQueryHandler());

        @Mock(strictness = Mock.Strictness.LENIENT)
        private EventProcessingConfigurer eventProcessingConfigurer;

        @BeforeEach
        void setUp() {
            when(componentsDiscovery.eventHandlers()).thenReturn(componentDiscovery);
            when(configurer.eventProcessing()).thenReturn(eventProcessingConfigurer);
        }

        @Test
        void enabledAggregateDiscovery() {
            when(componentDiscovery.enabled()).thenReturn(true);
            setup.configureEventHandlers(configurer, handlers);
            verify(eventProcessingConfigurer, Mockito.times(1)).registerEventHandler(any());
        }

        @Test
        void disabledAggregateDiscovery() {
            when(componentDiscovery.enabled()).thenReturn(false);
            setup.configureEventHandlers(configurer, handlers);
            verify(eventProcessingConfigurer, Mockito.never()).registerEventHandler(any());
        }
    }

    @Nested
    class QueryHandlersDiscovery {

        Set<Object> handlers = Set.of(new GiftcardQueryHandler());

        @BeforeEach
        void setUp() {
            when(componentsDiscovery.queryHandlers()).thenReturn(componentDiscovery);
        }

        @Test
        void enabledAggregateDiscovery() {
            when(componentDiscovery.enabled()).thenReturn(true);
            setup.configureQueryHandlers(configurer, handlers);
            verify(configurer, Mockito.times(1)).registerQueryHandler(any());
        }

        @Test
        void disabledAggregateDiscovery() {
            when(componentDiscovery.enabled()).thenReturn(false);
            setup.configureQueryHandlers(configurer, handlers);
            verify(configurer, Mockito.never()).registerQueryHandler(any());
        }
    }

    @Nested
    class SagaHandlersDiscovery {

        Set<Class<?>> handlers = Set.of(CardReturnSaga.class);

        @Mock(strictness = Mock.Strictness.LENIENT)
        private EventProcessingConfigurer eventProcessingConfigurer;

        @BeforeEach
        void setUp() {
            when(componentsDiscovery.sagaHandlers()).thenReturn(componentDiscovery);
            when(configurer.eventProcessing()).thenReturn(eventProcessingConfigurer);

        }

        @Test
        void enabledAggregateDiscovery() {
            when(componentDiscovery.enabled()).thenReturn(true);
            setup.configureSagas(configurer, handlers);
            verify(eventProcessingConfigurer, Mockito.times(1)).registerSaga(CardReturnSaga.class);
        }

        @Test
        void disabledAggregateDiscovery() {
            when(componentDiscovery.enabled()).thenReturn(false);
            setup.configureSagas(configurer, handlers);
            verify(eventProcessingConfigurer, Mockito.never()).registerSaga(CardReturnSaga.class);
        }
    }

}
