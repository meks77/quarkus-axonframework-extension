package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.stream.Collectors;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.DuplicateCommandHandlerResolution;
import org.axonframework.commandhandling.DuplicateCommandHandlerResolver;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;

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

    void configureCommandBus(Configurer configurer) {
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

    private void configureCommandBusUsingCustomProducer(Configurer configurer) {
        configurer.configureCommandBus(configuration -> commandBusProducer.get().createCommandBus(configuration));
    }

    private void configureCommandBusUsingBuilder(Configurer configurer) {
        configurer.configureCommandBus(this::createCommandBusWithBuilder);
    }

    private CommandBus createCommandBusWithBuilder(Configuration axonConfiguration) {
        return commandBusBuilder
                .duplicateCommandHandlerResolver(toResolver(duplicateCommandHandlerResolverType()))
                .build(axonConfiguration);
    }

    private DuplicateCommandHandlerResolverType duplicateCommandHandlerResolverType() {
        return axonConfiguration.commandBus().duplicateCommandHandlerResolverType();
    }

    private DuplicateCommandHandlerResolver toResolver(DuplicateCommandHandlerResolverType resolverType) {
        return switch (resolverType) {
            case logAndOverride -> DuplicateCommandHandlerResolution.logAndOverride();
            case rejectDuplicates -> DuplicateCommandHandlerResolution.rejectDuplicates();
            case silentOverride -> DuplicateCommandHandlerResolution.silentOverride();
        };
    }

}
