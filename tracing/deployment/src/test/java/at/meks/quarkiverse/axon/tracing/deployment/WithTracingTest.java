package at.meks.quarkiverse.axon.tracing.deployment;

import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class WithTracingTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase());

}
