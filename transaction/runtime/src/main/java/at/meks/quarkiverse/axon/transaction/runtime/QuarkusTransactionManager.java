package at.meks.quarkiverse.axon.transaction.runtime;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.common.jdbc.ConnectionExecutor;
import org.axonframework.common.jpa.EntityManagerExecutor;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.conversion.CachingSupplier;
import org.axonframework.messaging.core.unitofwork.ProcessingLifecycle;
import org.axonframework.messaging.core.unitofwork.transaction.Transaction;
import org.axonframework.messaging.core.unitofwork.transaction.TransactionManager;
import org.axonframework.messaging.core.unitofwork.transaction.jdbc.JdbcTransactionalExecutorProvider;
import org.axonframework.messaging.core.unitofwork.transaction.jpa.JpaTransactionalExecutorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class QuarkusTransactionManager implements TransactionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuarkusTransactionManager.class);

    @Inject
    RequestContextController requestContextController;

    @Inject
    Instance<EntityManagerProvider> entityManagerProvider;

    @Inject
    Instance<DataSource> dataSourceInstance;

    @Override
    public Transaction startTransaction() {
        return AxonTransaction.beginOrJoinTransaction(requestContextController);
    }

    @Override
    public void attachToProcessingLifecycle(ProcessingLifecycle processingLifecycle) {
        //        borrowed from spring extension org.axonframework.extension.spring.messaging.unitofwork.SpringTransactionManager#attachToProcessingLifecycle
        processingLifecycle.runOnPreInvocation(pc -> {
            Transaction transaction = startTransaction();
            try {
                Connection connection = dataSourceInstance.get().getConnection();
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Starting transaction with connection: {}", connection);
                }

                if (entityManagerProvider.isResolvable()) {
                    pc.putResource(
                            JpaTransactionalExecutorProvider.SUPPLIER_KEY,
                            CachingSupplier.of(() -> new EntityManagerExecutor(entityManagerProvider.get())));
                }
                if (dataSourceInstance.isResolvable()) {
                    pc.putResource(
                            JdbcTransactionalExecutorProvider.SUPPLIER_KEY,
                            CachingSupplier.of(() -> new ConnectionExecutor(() -> connection)));
                }

                pc.runOnCommit(p -> {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Closing connection: {}", connection);
                    }
                    if (dataSourceInstance.isResolvable()) {
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Committing transaction");
                    }
                    transaction.commit();
                });
                pc.onError((p, phase, e) -> transaction.rollback());
            } catch (SQLException e) {
                throw new IllegalStateException("Error happened while preparing the transaction", e);
            }
        });
    }
}
