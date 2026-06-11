package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.messaging.core.unitofwork.transaction.Transaction;
import org.axonframework.messaging.core.unitofwork.transaction.TransactionManager;

import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class NoTransactionManager implements TransactionManager {

    @Override
    public Transaction startTransaction() {
        return org.axonframework.common.transaction.NoTransactionManager.instance().startTransaction();
    }
}
