package at.meks.quarkiverse.axon.deployment.serializer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class CustomSerializerTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(javaArchiveBase()
            .addClasses(MyCustomJacksonSerializerProducer.class));

    @Override
    protected void assertOthers() {
        Assertions.assertTrue(MyCustomJacksonSerializerProducer.isCustomSerializerUsed());
    }
}
