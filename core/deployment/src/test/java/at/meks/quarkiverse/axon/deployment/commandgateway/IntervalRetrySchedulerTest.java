package at.meks.quarkiverse.axon.deployment.commandgateway;

import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

public class IntervalRetrySchedulerTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application()
            .withConfigurationResource("commandgateway/intervalretry.properties");

}
