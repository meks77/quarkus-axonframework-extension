package at.meks.quarkiverse.axon.tracing.deployment;

import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class DeactivatedTracing2Test extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/deactivatedTracing2.properties"), "application.properties"));

}
