package at.meks.quarkiverse.axon.runtime.defaults;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.*;

import java.util.function.Function;
import java.util.stream.Stream;

import jakarta.enterprise.inject.Instance;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.commandhandling.CommandBus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import at.meks.quarkiverse.axon.runtime.customizations.CommandBusBuilder;
import at.meks.quarkiverse.axon.runtime.customizations.CommandBusProducer;

@ExtendWith(MockitoExtension.class)
class CommandBusConfigurerTest {

    @Mock
    Instance<CommandBusProducer> commandBusProducer;

    @Mock
    EventSourcingConfigurer axonConfigurer;

    @InjectMocks
    CommandBusConfigurer configurer;

    private static class CommandBusProducer1 implements CommandBusProducer {
        @Override
        public CommandBus createCommandBus(Configuration configuration) {
            return null;
        }
    }

    private static class CommandBusProducer2 implements CommandBusProducer {
        @Override
        public CommandBus createCommandBus(Configuration configuration) {
            return null;
        }
    }

    @Nested
    class InvalidBeansProvided {

        @Test
        void producerIsAmbigious() {
            when(commandBusProducer.isAmbiguous())
                    .thenReturn(true);
            when(commandBusProducer.stream())
                    .thenReturn(Stream.of(new CommandBusProducer1(), new CommandBusProducer2()));

            assertThatIllegalStateException()
                    .isThrownBy(() -> configurer.configureCommandBus(axonConfigurer))
                    .withMessage("multiple commandBusProducers found: %s",
                            "at.meks.quarkiverse.axon.runtime.defaults.CommandBusConfigurerTest$CommandBusProducer1, " +
                                    "at.meks.quarkiverse.axon.runtime.defaults.CommandBusConfigurerTest$CommandBusProducer2");
        }

    }

}
