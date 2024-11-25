package at.meks.quarkiverse.axon.eventprocessor.persistentstream.deployment;

import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class WithDefaultsTest extends PersistentStreamProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = JavaArchiveTest.application(JavaArchiveTest.javaArchiveBase());

}
