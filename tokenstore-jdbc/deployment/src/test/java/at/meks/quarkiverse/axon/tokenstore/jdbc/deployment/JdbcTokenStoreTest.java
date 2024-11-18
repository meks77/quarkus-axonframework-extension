package at.meks.quarkiverse.axon.tokenstore.jdbc.deployment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import jakarta.inject.Inject;

import org.axonframework.eventhandling.EventProcessor;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public abstract class JdbcTokenStoreTest extends JavaArchiveTest {

    static QuarkusUnitTest jdbcStoreApplication(String applicationPropertiesFilename) {
        return JavaArchiveTest.application(
                JavaArchiveTest.javaArchiveBase().addAsResource(JavaArchiveTest.propertiesFile(
                        applicationPropertiesFilename),
                        "application.properties"));
    }

    @Inject
    DataSource dataSource;

    @Override
    protected final void assertConfiguration(EventProcessor eventProcessor) {
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
