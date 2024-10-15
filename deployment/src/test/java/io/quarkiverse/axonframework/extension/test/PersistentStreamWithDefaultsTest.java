package io.quarkiverse.axonframework.extension.test;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class PersistentStreamWithDefaultsTest extends AbstractConfigurationTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(javaArchiveBase()
            .addAsResource(propertiesFile("/persistentstreams/withDefaults.properties"), "application.properties"));

}
