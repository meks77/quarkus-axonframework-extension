package at.meks.quarkiverse.axon.sagastore.jdbc.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import at.meks.quarkiverse.shared.test.jdbc.SqlExecutor;
import io.quarkus.test.QuarkusUnitTest;

public class JdbcSagaStoreTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application()
            .withConfigurationResource("application.properties");

    @Inject
    SqlExecutor sqlExecutor;

    @Override
    protected void prepareForTest() {
        sqlExecutor.executeSqlUpdate("delete from JdbcSagaEntry");
        sqlExecutor.executeSqlUpdate("delete from JdbcAssociationValueEntry");
    }

    @Override
    protected final void assertOthers() {
        // One saga is started and ended -> it's deleted from the database
        // One saga is only started -> saga must exist in the database
        delayedAssert(() -> assertThat(countTableRecords("JdbcSagaEntry")).isEqualTo(1));
        delayedAssert(() -> assertThat(countTableRecords("JdbcAssociationValueEntry")).isEqualTo(1));
    }

    private int countTableRecords(String tableName) {
        return sqlExecutor.getSingleValueFromSql("SELECT count(*) from " + tableName, resultSet -> resultSet.getInt(1));
    }

}
