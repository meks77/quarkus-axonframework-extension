package at.meks.quarkiverse.axon.tokenstore.jpa.deployment;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class WithDefaultsTest extends JpaTokenstoreTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(javaArchiveBase().addAsResource(
            propertiesFile("/defaults.properties"), "application.properties"));

}
