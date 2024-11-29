package at.meks.quarkiverse.axon.eventstore.jdbc.deployment;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

/**
 * no possibility found to verify that configuration is used as expected.
 */
public class AllPropertiesChangedTest extends JdbcEventstoreTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(javaArchiveBase()
            .addAsResource(propertiesFile("/changed.properties"), "application.properties"));

}
