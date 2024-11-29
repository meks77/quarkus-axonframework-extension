package at.meks.quarkiverse.axon.eventstore.jdbc.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import jakarta.inject.Inject;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;

public abstract class JdbcEventstoreTest extends JavaArchiveTest {

    @Inject
    DataSource dataSource;

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
        int eventCount;
        try (var connection = dataSource.getConnection();
                var statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT count(*) from " + tableName)) {
            resultSet.next();
            eventCount = resultSet.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return eventCount;
    }

}
