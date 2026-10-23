package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Reserve;

import org.axonframework.messaging.core.unitofwork.transaction.Transaction;
import org.axonframework.messaging.core.unitofwork.transaction.TransactionManager;

@ApplicationScoped
@Reserve
@Priority(0)
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
