package at.meks.quarkiverse.axon.runtime.defaults;

import java.util.Optional;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.axonframework.common.configuration.AxonConfiguration;
//import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.messaging.commandhandling.CommandBus;
import org.axonframework.messaging.commandhandling.SimpleCommandBus;
import org.axonframework.messaging.commandhandling.retry.RetryingCommandBus;
import org.axonframework.messaging.core.unitofwork.UnitOfWorkFactory;

import at.meks.quarkiverse.axon.runtime.customizations.CommandBusBuilder;
import io.quarkus.arc.DefaultBean;

@Dependent
@DefaultBean
public class LocalCommandBusBuilder implements CommandBusBuilder {
    @Inject
    private RetrySchedulerConfigurer retrySchedulerConfigurer;

    //    private DuplicateCommandHandlerResolver resolver;

    //    public CommandBusBuilder duplicateCommandHandlerResolver(DuplicateCommandHandlerResolver resolver) {
    //        this.resolver = resolver;
    //        return this;
    //    }

    public CommandBus build(AxonConfiguration config) {
        SimpleCommandBus simpleCommandBus = new SimpleCommandBus(config.getComponent(UnitOfWorkFactory.class));
        Optional<CommandBus> enhancedCommandBus = retrySchedulerConfigurer.retryScheduler()
                .map(retryScheduler -> new RetryingCommandBus(simpleCommandBus, retryScheduler));
        return enhancedCommandBus.orElse(simpleCommandBus);
        //        return new RetryingCommandBus(simpleCommandBus, retryScheduler);
        //                SimpleCommandBus.Builder builder = SimpleCommandBus.builder()
        //                        .transactionManager(config.getComponent(TransactionManager.class))
        //                        .spanFactory(DefaultCommandBusSpanFactory.builder().spanFactory(config.spanFactory())
        //                                .distributedInSameTrace(true).build())
        //                        .messageMonitor(config.messageMonitor(SimpleCommandBus.class, "commandBus"));
        //                Optional.ofNullable(resolver).ifPresent(builder::duplicateCommandHandlerResolver);
        //                return builder.build();
    }
}
