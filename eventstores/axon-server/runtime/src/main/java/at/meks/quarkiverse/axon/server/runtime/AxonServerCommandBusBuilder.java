package at.meks.quarkiverse.axon.server.runtime;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.messaging.commandhandling.CommandBus;

import at.meks.quarkiverse.axon.runtime.customizations.CommandBusBuilder;
import at.meks.quarkiverse.axon.runtime.defaults.LocalCommandBusBuilder;
import io.axoniq.framework.messaging.commandhandling.distributed.CommandBusConnector;
import io.axoniq.framework.messaging.commandhandling.distributed.DistributedCommandBus;
import io.axoniq.framework.messaging.commandhandling.distributed.DistributedCommandBusConfiguration;
import io.quarkus.arc.DefaultBean;

@Dependent
@DefaultBean
@Priority(5)
public class AxonServerCommandBusBuilder implements CommandBusBuilder {

    @Inject
    LocalCommandBusBuilder localCommandBusBuilder;

    @Override
    public CommandBus build(Configuration configuration) {
        var localCommandBus = localCommandBusBuilder.build(configuration);
        return new DistributedCommandBus(localCommandBus, configuration.getComponent(CommandBusConnector.class),
                DistributedCommandBusConfiguration.DEFAULT);
    }

}
