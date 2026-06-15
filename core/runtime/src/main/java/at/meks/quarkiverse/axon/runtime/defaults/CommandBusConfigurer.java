package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.stream.Collectors;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.commandhandling.CommandBus;

//import org.axonframework.messaging.commandhandling.DuplicateCommandHandlerResolution;
//import org.axonframework.messaging.commandhandling.DuplicateCommandHandlerResolver;
import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.conf.DuplicateCommandHandlerResolverType;
import at.meks.quarkiverse.axon.runtime.customizations.CommandBusBuilder;
import at.meks.quarkiverse.axon.runtime.customizations.CommandBusProducer;

@Dependent
public class CommandBusConfigurer {

    @Inject
    Instance<CommandBusProducer> commandBusProducer;

    @Inject
    CommandBusBuilder commandBusBuilder;

    @Inject
    AxonConfiguration axonConfiguration;

    void configureCommandBus(EventSourcingConfigurer configurer) {
        verifyProvidedBeans();
        if (commandBusProducer.isResolvable()) {
            configureCommandBusUsingCustomProducer(configurer);
        } else {
            // TODO: Configure CommandBus if still necessary or remove it
            //            configureCommandBusUsingBuilder(configurer);
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

    // TODO: Configure CommandBus if still necessary or remove it
    //    private void configureCommandBusUsingBuilder(EventSourcingConfigurer configurer) {
    //        configurer.configureCommandBus(this::createCommandBusWithBuilder);
    //    }

    // TODO: Configure CommandBus if still necessary or remove it
    private CommandBus createCommandBusWithBuilder(org.axonframework.common.configuration.AxonConfiguration axonConfiguration) {
        return commandBusBuilder
                //                .duplicateCommandHandlerResolver(toResolver(duplicateCommandHandlerResolverType()))
                .build(axonConfiguration);
    }

    // TODO: Configure CommandBus if still necessary or remove it
    private DuplicateCommandHandlerResolverType duplicateCommandHandlerResolverType() {
        return axonConfiguration.commandBus().duplicateCommandHandlerResolverType();
    }

    // TODO: Configure CommandBus if still necessary or remove it
    //    private DuplicateCommandHandlerResolver toResolver(DuplicateCommandHandlerResolverType resolverType) {
    //        return switch (resolverType) {
    //            case logAndOverride -> DuplicateCommandHandlerResolution.logAndOverride();
    //            case rejectDuplicates -> DuplicateCommandHandlerResolution.rejectDuplicates();
    //            case silentOverride -> DuplicateCommandHandlerResolution.silentOverride();
    //        };
    //    }

}
