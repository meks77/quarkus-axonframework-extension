package at.meks.quarkiverse.axonframework.extension.test.streamingprocessors.tep;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import jakarta.inject.Inject;

import org.axonframework.eventhandling.TrackingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class JdbcTokenstoreTest extends TrackingProcessorTest {

    @Inject
    DataSource dataSource;

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/streamingeventprocessors/tep/jdbctokenstore.properties"),
                    "application.properties"));

    @Override
    protected void assertConfiguration(TrackingEventProcessor eventProcessor) {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet rset = statement.executeQuery(
                        "select * from TokenEntry")) {
            assertTrue(rset.next());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
