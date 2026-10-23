package at.meks.quarkiverse.axon.eventstore.jdbc.deployment;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;

public class WithDefaultsTest extends JdbcEventstoreTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application()
            .withConfigurationResource("application.properties");

}
