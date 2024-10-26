package io.quarkiverse.axonframework.extension.runtime;

import jakarta.enterprise.context.control.RequestContextController;

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
        if (!QuarkusTransaction.isActive()) {
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
        if (QuarkusTransaction.isActive() && !joined) {
            LOG.trace("commit transaction");
            QuarkusTransaction.commit();
        } else {
            LOG.tracef("avoid commit. transaction active: %s; transaction joined: %s", QuarkusTransaction.isActive(), joined);
        }
    }

    @Override
    public void rollback() {
        if (QuarkusTransaction.isActive()) {
            LOG.trace("rollback transaction");
            QuarkusTransaction.rollback();
        } else {
            LOG.tracef("avoid rollback. transaction active: %s", QuarkusTransaction.isActive());
        }
    }
}
