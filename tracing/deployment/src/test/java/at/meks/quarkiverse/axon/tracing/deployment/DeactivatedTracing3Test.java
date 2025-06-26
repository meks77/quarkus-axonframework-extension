package at.meks.quarkiverse.axon.tracing.deployment;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

import org.junit.jupiter.api.extension.*;

public class DeactivatedTracing3Test extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/deactivatedTracing3.properties"), "application.properties"));

}
