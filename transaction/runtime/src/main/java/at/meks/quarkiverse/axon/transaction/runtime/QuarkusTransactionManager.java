package at.meks.quarkiverse.axon.transaction.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.common.jpa.EntityManagerExecutor;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.conversion.CachingSupplier;
import org.axonframework.messaging.core.unitofwork.ProcessingLifecycle;
import org.axonframework.messaging.core.unitofwork.transaction.Transaction;
import org.axonframework.messaging.core.unitofwork.transaction.TransactionManager;
import org.axonframework.messaging.core.unitofwork.transaction.jpa.JpaTransactionalExecutorProvider;

@ApplicationScoped
public class QuarkusTransactionManager implements TransactionManager {

    @Inject
    RequestContextController requestContextController;

    @Inject
    Instance<EntityManagerProvider> entityManagerProvider;

    @Override
    public Transaction startTransaction() {
        return AxonTransaction.beginOrJoinTransaction(requestContextController);
    }

    @Override
    public void attachToProcessingLifecycle(ProcessingLifecycle processingLifecycle) {
        //        borrowed from spring extension org.axonframework.extension.spring.messaging.unitofwork.SpringTransactionManager#attachToProcessingLifecycle
        processingLifecycle.runOnPreInvocation(pc -> {
            Transaction transaction = startTransaction();

            if (entityManagerProvider.isResolvable()) {
                pc.putResource(
                        JpaTransactionalExecutorProvider.SUPPLIER_KEY,
                        CachingSupplier.of(() -> new EntityManagerExecutor(entityManagerProvider.get())));
            }

            //            TODO no connection provider here
            //            if (connectionProvider != null) {
            //                pc.putResource(
            //                        JdbcTransactionalExecutorProvider.SUPPLIER_KEY,
            //                        CachingSupplier.of(() -> new ConnectionExecutor(connectionProvider))
            //                );
            //            }

            pc.runOnCommit(p -> transaction.commit());
            pc.onError((p, phase, e) -> transaction.rollback());
        });
    }
}
