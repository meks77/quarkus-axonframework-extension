package at.meks.quarkiverse.axon.transaction.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.inject.Inject;

import org.axonframework.common.transaction.Transaction;
import org.axonframework.common.transaction.TransactionManager;

@ApplicationScoped
public class JdbcTransactionManager implements TransactionManager {

    @Inject
    RequestContextController requestContextController;

    @Override
    public Transaction startTransaction() {
        return AxonTransaction.beginOrJoinTransaction(requestContextController);
    }

}
