package at.meks.quarkiverse.axon.tokenstore.jdbc.deployment;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class PooledEventProcessorTest extends JdbcTokenStoreTest {

    @RegisterExtension
    static final QuarkusUnitTest config = jdbcStoreApplication("/pooledEventProcessor.properties");

}
