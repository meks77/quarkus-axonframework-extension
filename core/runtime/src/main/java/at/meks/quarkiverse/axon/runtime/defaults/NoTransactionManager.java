package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.messaging.core.unitofwork.transaction.Transaction;
import org.axonframework.messaging.core.unitofwork.transaction.TransactionManager;

import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class NoTransactionManager implements TransactionManager {

    private static final Transaction singletonNoTransaction = new Transaction() {

        @Override
        public void commit() {

        }

        @Override
        public void rollback() {

        }
    };

    @Override
    public Transaction startTransaction() {
        return singletonNoTransaction;
    }
}
