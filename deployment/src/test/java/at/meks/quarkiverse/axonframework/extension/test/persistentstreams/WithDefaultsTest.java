package at.meks.quarkiverse.axonframework.extension.test.persistentstreams;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class WithDefaultsTest extends PersistentStreamProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/persistentstreams/withDefaults.properties"),
                    "application.properties"));

}
