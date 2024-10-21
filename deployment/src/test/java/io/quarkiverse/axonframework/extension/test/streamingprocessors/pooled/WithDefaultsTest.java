package io.quarkiverse.axonframework.extension.test.streamingprocessors.pooled;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.axonframework.extension.test.AbstractConfigurationTest;
import io.quarkus.test.QuarkusUnitTest;

public class WithDefaultsTest extends AbstractConfigurationTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/streamingeventprocessors/pooled/withDefaults.properties"),
                    "application.properties"));

}
