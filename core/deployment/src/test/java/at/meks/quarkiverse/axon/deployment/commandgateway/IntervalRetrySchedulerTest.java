package at.meks.quarkiverse.axon.deployment.commandgateway;

import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class IntervalRetrySchedulerTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(javaArchiveBase()
            .addAsResource(propertiesFile("/commandgateway/intervalretry.properties"),
                    "application.properties"));

}
