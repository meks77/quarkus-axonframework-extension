package io.quarkiverse.axonframework.extension.test.streamingprocessors.pooled;

import static org.assertj.core.api.Assertions.assertThat;
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
                ResultSet rset = statement.executeQuery("select * from TokenEntry where processorName like 'quarkus%'")) {
            assertTrue(rset.next());
            String token = rset.getString("token");
            assertThat(token).isNotNull().matches("\\{\"globalIndex\":.*}");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
