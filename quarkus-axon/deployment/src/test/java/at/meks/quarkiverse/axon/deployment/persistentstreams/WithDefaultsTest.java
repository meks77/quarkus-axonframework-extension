package at.meks.quarkiverse.axon.deployment.persistentstreams;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class WithDefaultsTest extends PersistentStreamProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/persistentstreams/withDefaults.properties"),
                    "application.properties"));

}
