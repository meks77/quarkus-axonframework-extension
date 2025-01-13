package at.meks.quarkiverse.axon.sagastore.jdbc.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import at.meks.quarkiverse.shared.test.jdbc.SqlExecutor;
import io.quarkus.test.QuarkusUnitTest;

public class JdbcSagaStoreTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(javaArchiveBase()
            .addAsResource(propertiesFile("/application.properties"), "application.properties"));

    @Inject
    SqlExecutor sqlExecutor;

    @Override
    protected void prepareForTest() {
        sqlExecutor.executeSqlUpdate("delete from JdbcSagaEntry");
    }

    @Override
    protected final void assertOthers() {
        // One saga is started and ended -> it's deleted from the database
        // One saga is only started -> saga must exist in the database
        delayedAssert(() -> assertThat(eventCountInDatabase()).isEqualTo(1));
    }

    private int eventCountInDatabase() {
        return countTableRecords();
    }

    private int countTableRecords() {
        int eventCount;
        eventCount = sqlExecutor.getSingleValueFromSql("SELECT count(*) from JdbcSagaEntry", resultSet -> resultSet.getInt(1));
        return eventCount;
    }

}
