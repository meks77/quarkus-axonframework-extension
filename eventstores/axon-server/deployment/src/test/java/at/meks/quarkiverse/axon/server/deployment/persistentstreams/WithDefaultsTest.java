package at.meks.quarkiverse.axon.server.deployment.persistentstreams;

import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class WithDefaultsTest extends PersistentStreamProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = JavaArchiveTest.application()
            .withConfigurationResource("persistenstreams/withDefaults.properties");

}
