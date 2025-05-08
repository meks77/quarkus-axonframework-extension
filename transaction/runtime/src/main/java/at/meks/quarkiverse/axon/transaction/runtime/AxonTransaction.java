package at.meks.quarkiverse.axon.transaction.runtime;

import jakarta.enterprise.context.control.RequestContextController;
import jakarta.transaction.Status;

import org.axonframework.common.transaction.Transaction;
import org.jboss.logging.Logger;

import io.quarkus.narayana.jta.QuarkusTransaction;

public class AxonTransaction implements Transaction {

    private static final AxonTransaction instance = new AxonTransaction(false);

    private static final AxonTransaction joinedInstance = new AxonTransaction(true);

    public static final String LOGGER_NAME = "axon-transaction";

    private static final Logger LOG = Logger.getLogger(LOGGER_NAME);

    private final boolean joined;

    static AxonTransaction beginOrJoinTransaction(RequestContextController requestContextController) {
        if (!isTransactionStarted()) {
            LOG.trace("Begin transaction");
            requestContextController.activate();
            QuarkusTransaction.begin();
            return AxonTransaction.newTransaction();
        } else {
            LOG.trace("join transaction");
            QuarkusTransaction.joiningExisting();
            return AxonTransaction.joinedTransaction();
        }
    }

    private static boolean isTransactionStarted() {
        return QuarkusTransaction.getStatus() != Status.STATUS_NO_TRANSACTION;
    }

    static AxonTransaction newTransaction() {
        return instance;
    }

    static AxonTransaction joinedTransaction() {
        return joinedInstance;
    }

    private AxonTransaction(boolean joined) {
        this.joined = joined;
    }

    @Override
    public void commit() {
        if (isTransactionStarted() && !joined) {
            LOG.trace("commit transaction");
            QuarkusTransaction.commit();
        } else {
            LOG.tracef("avoid commit. transaction was started: %s; transaction joined: %s", isTransactionStarted(), joined);
        }
    }

    @Override
    public void rollback() {
        if (isTransactionStarted()) {
            LOG.trace("rollback transaction");
            QuarkusTransaction.rollback();
        } else {
            LOG.tracef("avoid rollback. transaction started: %s", isTransactionStarted());
        }
    }
}
