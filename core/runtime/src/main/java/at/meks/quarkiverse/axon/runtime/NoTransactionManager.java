package at.meks.quarkiverse.axon.runtime;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.common.transaction.Transaction;
import org.axonframework.common.transaction.TransactionManager;

import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class NoTransactionManager implements TransactionManager {

    @Override
    public Transaction startTransaction() {
        return org.axonframework.common.transaction.NoTransactionManager.instance().startTransaction();
    }
}
