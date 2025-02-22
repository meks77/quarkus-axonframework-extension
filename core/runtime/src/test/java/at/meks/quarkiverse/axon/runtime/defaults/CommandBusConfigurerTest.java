package at.meks.quarkiverse.axon.runtime.defaults;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.*;

import java.util.function.Function;
import java.util.stream.Stream;

import jakarta.enterprise.inject.Instance;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
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
    CommandBusBuilder commandBusBuilder;

    @Mock
    Instance<CommandBusProducer> commandBusProducer;

    @Mock
    Configurer axonConfigurer;

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

    @Test
    void producerIsResolvable() {
        when(commandBusProducer.isResolvable()).thenReturn(true);
        CommandBusProducer expectedCommandProducer = Mockito.mock(CommandBusProducer.class);
        when(commandBusProducer.get()).thenReturn(expectedCommandProducer);

        configurer.configureCommandBus(axonConfigurer);

        var functionCaptor = commandBusFunctionCaptor();
        verify(axonConfigurer).configureCommandBus(functionCaptor.capture());
        functionCaptor.getValue().apply(Mockito.mock(Configuration.class));
        verify(expectedCommandProducer).createCommandBus(Mockito.any());

        verifyNoInteractions(commandBusBuilder);
    }

    private ArgumentCaptor<Function<Configuration, CommandBus>> commandBusFunctionCaptor() {
        //noinspection unchecked
        return ArgumentCaptor.forClass(Function.class);
    }

}