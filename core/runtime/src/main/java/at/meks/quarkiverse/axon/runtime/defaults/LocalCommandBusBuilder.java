package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.Optional;

import jakarta.enterprise.context.Dependent;

import org.axonframework.common.configuration.Configuration;
import org.axonframework.messaging.commandhandling.CommandBus;
import org.axonframework.messaging.commandhandling.SimpleCommandBus;
import org.axonframework.messaging.commandhandling.retry.RetryingCommandBus;
import org.axonframework.messaging.core.unitofwork.UnitOfWorkFactory;

import at.meks.quarkiverse.axon.runtime.customizations.CommandBusBuilder;
import io.quarkus.arc.DefaultBean;

@Dependent
@DefaultBean
public class LocalCommandBusBuilder implements CommandBusBuilder {

    private final RetrySchedulerConfigurer retrySchedulerConfigurer;

    public LocalCommandBusBuilder(RetrySchedulerConfigurer retrySchedulerConfigurer) {
        this.retrySchedulerConfigurer = retrySchedulerConfigurer;
    }

    public CommandBus build(Configuration config) {
        Optional<CommandBus> enhancedCommandBus = retrySchedulerConfigurer.retryScheduler()
                .map(retryScheduler -> new RetryingCommandBus(
                        new SimpleCommandBus(config.getComponent(UnitOfWorkFactory.class)), retryScheduler));
        return enhancedCommandBus
                .orElse(new SimpleCommandBus(config.getComponent(UnitOfWorkFactory.class)));
    }
}
