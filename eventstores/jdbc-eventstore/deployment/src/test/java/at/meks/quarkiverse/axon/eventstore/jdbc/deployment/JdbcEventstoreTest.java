package at.meks.quarkiverse.axon.eventstore.jdbc.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import at.meks.quarkiverse.shared.test.jdbc.SqlExecutor;

public abstract class JdbcEventstoreTest extends JavaArchiveTest {

    @Inject
    SqlExecutor sqlExecutor;

    @Override
    protected final void assertOthers() {
        assertThat(eventCountInDatabase()).isGreaterThanOrEqualTo(2);
    }

    private int eventCountInDatabase() {
        return countTableRecords("JdbcDomainEventEntry");
    }

    private int countTableRecords(String tableName) {
        return sqlExecutor.getSingleValueFromSql("SELECT count(*) from " + tableName, resultSet -> resultSet.getInt(1));
    }

}
