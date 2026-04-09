package at.meks.quarkiverse.axon.eventstore.jdbc.deployment;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class WithDefaultsTest extends JdbcEventstoreTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application()
            .withConfigurationResource("application.properties");

}
