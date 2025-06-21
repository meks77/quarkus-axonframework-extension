package at.meks.quarkiverse.axon.server.runtime;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.axonserver.connector.command.AxonServerCommandBus;
import org.axonframework.axonserver.connector.command.CommandPriorityCalculator;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.DefaultCommandBusSpanFactory;
import org.axonframework.commandhandling.DuplicateCommandHandlerResolver;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.config.Configuration;

import at.meks.quarkiverse.axon.runtime.customizations.CommandBusBuilder;
import at.meks.quarkiverse.axon.runtime.defaults.LocalCommandBusBuilder;
import io.quarkus.arc.DefaultBean;

@Dependent
@DefaultBean
@Priority(5)
public class AxonServerCommandBusBuilder implements CommandBusBuilder {

    @Inject
    LocalCommandBusBuilder localCommandBusBuilder;

    private DuplicateCommandHandlerResolver duplicateResolver;

    @Override
    public CommandBusBuilder duplicateCommandHandlerResolver(DuplicateCommandHandlerResolver resolver) {
        this.duplicateResolver = resolver;
        return this;
    }

    @Override
    public CommandBus build(Configuration configuration) {
        AxonServerConfiguration axonServerConfiguration = configuration.getComponent(AxonServerConfiguration.class);
        return AxonServerCommandBus.builder()
                .localSegment(
                        localCommandBusBuilder.duplicateCommandHandlerResolver(duplicateResolver).build(configuration))
                .configuration(axonServerConfiguration)
                .axonServerConnectionManager(configuration.getComponent(AxonServerConnectionManager.class))
                .defaultContext(axonServerConfiguration.getContext())
                .serializer(configuration.serializer())
                .routingStrategy(AnnotationRoutingStrategy.defaultStrategy())
                .priorityCalculator(CommandPriorityCalculator.defaultCommandPriorityCalculator())
                .spanFactory(DefaultCommandBusSpanFactory.builder().spanFactory(
                        configuration.spanFactory()).distributedInSameTrace(true).build())
                .build();
    }

}
