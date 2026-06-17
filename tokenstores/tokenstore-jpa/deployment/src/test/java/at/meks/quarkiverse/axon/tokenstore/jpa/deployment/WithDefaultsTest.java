package at.meks.quarkiverse.axon.tokenstore.jpa.deployment;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;

public class WithDefaultsTest extends JpaTokenstoreTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application()
            .withConfigurationResource("defaults.properties");

}
