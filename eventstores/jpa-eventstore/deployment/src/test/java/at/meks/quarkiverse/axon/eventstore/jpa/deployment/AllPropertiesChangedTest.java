package at.meks.quarkiverse.axon.eventstore.jpa.deployment;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;

/**
 * no possibility found to verify that configuration is used as expected.
 */
public class AllPropertiesChangedTest extends JpaEventstoreTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application()
            .withConfigurationResource("changed.properties");

}
