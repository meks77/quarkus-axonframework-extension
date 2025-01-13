package at.meks.quarkiverse.axon.eventstore.jdbc.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;

public abstract class JdbcEventstoreTest extends JavaArchiveTest {

    @Inject
    at.meks.quarkiverse.shared.test.jdbc.SqlExecutor sqlExecutor;

    @Override
    protected final void assertOthers() {
        assertThat(eventCountInDatabase()).isGreaterThanOrEqualTo(2);
        assertThat(snapshotCountInDatabase()).isGreaterThanOrEqualTo(0);
    }

    private int snapshotCountInDatabase() {
        return countTableRecords("JdbcSnapshotEventEntry");
    }

    private int eventCountInDatabase() {
        return countTableRecords("JdbcDomainEventEntry");
    }

    private int countTableRecords(String tableName) {
        return sqlExecutor.getSingleValueFromSql("SELECT count(*) from " + tableName, resultSet -> resultSet.getInt(1));
    }

}
