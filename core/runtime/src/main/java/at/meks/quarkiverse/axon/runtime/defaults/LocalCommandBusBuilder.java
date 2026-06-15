package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.enterprise.context.Dependent;

import org.axonframework.common.configuration.AxonConfiguration;
//import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.messaging.commandhandling.CommandBus;

import at.meks.quarkiverse.axon.runtime.customizations.CommandBusBuilder;
import io.quarkus.arc.DefaultBean;

@Dependent
@DefaultBean
public class LocalCommandBusBuilder implements CommandBusBuilder {

    //    private DuplicateCommandHandlerResolver resolver;

    //    public CommandBusBuilder duplicateCommandHandlerResolver(DuplicateCommandHandlerResolver resolver) {
    //        this.resolver = resolver;
    //        return this;
    //    }

    public CommandBus build(AxonConfiguration config) {
        //        SimpleCommandBus.Builder builder = SimpleCommandBus.builder()
        //                .transactionManager(config.getComponent(TransactionManager.class))
        //                .spanFactory(DefaultCommandBusSpanFactory.builder().spanFactory(config.spanFactory())
        //                        .distributedInSameTrace(true).build())
        //                .messageMonitor(config.messageMonitor(SimpleCommandBus.class, "commandBus"));
        //        Optional.ofNullable(resolver).ifPresent(builder::duplicateCommandHandlerResolver);
        //        return builder.build();
        return null;
    }
}
