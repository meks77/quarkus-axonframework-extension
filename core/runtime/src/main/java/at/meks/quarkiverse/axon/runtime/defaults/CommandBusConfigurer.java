package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.stream.Collectors;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.commandhandling.CommandBus;

import at.meks.quarkiverse.axon.runtime.customizations.CommandBusBuilder;
import at.meks.quarkiverse.axon.runtime.customizations.CommandBusProducer;

@Dependent
public class CommandBusConfigurer {

    @Inject
    Instance<CommandBusProducer> commandBusProducer;

    @Inject
    CommandBusBuilder commandBusBuilder;

    void configureCommandBus(EventSourcingConfigurer configurer) {
        verifyProvidedBeans();
        if (commandBusProducer.isResolvable()) {
            configureCommandBusUsingCustomProducer(configurer);
        } else {
            configureCommandBusUsingBuilder(configurer);
        }
    }

    private void verifyProvidedBeans() {
        if (commandBusProducer.isAmbiguous()) {
            throw new IllegalStateException(
                    "multiple commandBusProducers found: %s"
                            .formatted(commandBusProducer.stream().map(Object::getClass).map(Class::getName).collect(
                                    Collectors.joining(", "))));
        }
    }

    private void configureCommandBusUsingCustomProducer(EventSourcingConfigurer configurer) {
        configurer.componentRegistry(
                cr -> cr.registerComponent(CommandBus.class, config -> commandBusProducer.get().createCommandBus(config)));
    }

    private void configureCommandBusUsingBuilder(EventSourcingConfigurer configurer) {
        configurer.messaging(messagingConfigurer -> messagingConfigurer.registerCommandBus(this::createCommandBusWithBuilder));
    }

    private CommandBus createCommandBusWithBuilder(Configuration axonConfiguration) {
        return commandBusBuilder
                .build(axonConfiguration);
    }

}
