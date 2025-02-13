package at.meks.quarkiverse.axon.deployment.commandgateway;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.commandhandling.gateway.RetryScheduler;
import org.axonframework.config.Configuration;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class CustomRetrySchedulerTest extends JavaArchiveTest {

    @Inject
    RetryScheduler retryScheduler;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> JavaArchiveTest.javaArchiveBase()
                    .addClasses(MyRetrySchedulerProducer.class));

    public static class MyRetrySchedulerProducer {

        @Produces
        @ApplicationScoped
        public RetryScheduler retryScheduler() {
            return Mockito.mock(RetryScheduler.class);
        }

    }

    @Override
    protected void assertConfiguration(Configuration configuration) {
        DefaultCommandGateway commandGateway = (DefaultCommandGateway) configuration.commandGateway();
        try {
            Object actualRetryScheduler = FieldUtils.readField(commandGateway, "retryScheduler", true);
            assertThat(actualRetryScheduler).isSameAs(retryScheduler);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
