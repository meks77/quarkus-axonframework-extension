package at.meks.quarkiverse.axon.eventstore.jpa.deployment;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class WithDefaultsTest extends JpaEventstoreTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(javaArchiveBase()
            .addAsResource(propertiesFile("/application.properties"), "application.properties"));

}
