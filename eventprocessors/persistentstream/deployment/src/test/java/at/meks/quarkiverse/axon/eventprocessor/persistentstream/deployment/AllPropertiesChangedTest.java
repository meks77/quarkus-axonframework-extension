package at.meks.quarkiverse.axon.eventprocessor.persistentstream.deployment;

import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

/**
 * There is nothing what can be asserted, except that commands and events are processed.
 * Therefor all config properties, except the context, are changed to a different value.
 * <p>
 * The context can't be changed, because the axon without enterprise license doesn't supports just one context.
 */
public class AllPropertiesChangedTest extends PersistentStreamProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = JavaArchiveTest.application(
            JavaArchiveTest.javaArchiveBase().addAsResource(
                    JavaArchiveTest.propertiesFile("/propertiesChanged.properties"), "application.properties"));

}
