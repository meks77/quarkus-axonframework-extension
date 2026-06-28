package at.meks.quarkiverse.axon.deployment.commandgateway;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.axonframework.common.configuration.Configuration;
import org.axonframework.messaging.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.core.retry.RetryScheduler;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

public class CustomRetrySchedulerTest extends JavaArchiveTest {

    @Inject
    RetryScheduler retryScheduler;

    @RegisterExtension
    static final QuarkusExtensionTest config = application(javaArchiveBase()
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
        CommandGateway commandGateway = configuration.getComponent(CommandGateway.class);
        try {
            Object delegatingCommandGateway = FieldUtils.readField(commandGateway, "delegate", true);
            Object commandBus = FieldUtils.readField(delegatingCommandGateway, "commandBus", true);
            Object retryingCommandBus = FieldUtils.readField(commandBus, "delegate", true);
            Object actualRetryScheduler = FieldUtils.readField(retryingCommandBus, "retryScheduler", true);
            assertThat(actualRetryScheduler).isSameAs(retryScheduler);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
