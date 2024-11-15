package at.meks.quarkiverse.axon.deployment.streamingprocessors.pooled;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import jakarta.inject.Inject;

import org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class JdbcTokenstoreTest extends PooledProcessorTest {

    @Inject
    DataSource dataSource;

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/streamingeventprocessors/pooled/jdbctokenstore.properties"),
                    "application.properties"));

    @Override
    protected void assertPooledConfiguration(PooledStreamingEventProcessor eventProcessor) {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet rset = statement.executeQuery("select * from TokenEntry")) {
            assertTrue(rset.next());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
