package io.quarkiverse.axonframework.extension.test.persistentstreams;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

/**
 * There is nothing what can be asserted, except that commands and events are processed.
 * Therefor all config properties, except the context, are changed to a different value.
 * <p>
 * The context can't be changed, because the axon without enterprise license doesn't supports just one context.
 */
public class AllPropertiesChangedTest extends PersistentStreamProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/persistentstreams/propertiesChanged.properties"),
                    "application.properties"));

}
