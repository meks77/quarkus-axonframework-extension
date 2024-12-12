package at.meks.quarkiverse.axon.deployment.interceptor.event;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.slf4j.Logger;

import at.meks.quarkiverse.axon.runtime.customizations.EventDispatchInterceptorsProducer;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class OneDispatchInterceptorProducerTest extends JavaArchiveTest {

    private static final Logger LOGGER = Mockito.mock(Logger.class);

    public static class LoggerProducer {

        @Produces
        public Logger logger() {
            return LOGGER;
        }
    }

    @ApplicationScoped
    public static class InterceptorsProducer implements EventDispatchInterceptorsProducer {

        private final Logger logger;

        public InterceptorsProducer(Logger logger) {
            this.logger = logger;
        }

        @Override
        public List<MessageDispatchInterceptor<EventMessage<?>>> createDispatchInterceptor() {
            return List.of(interceptor("Interceptor 1"), interceptor("Interceptor 2"));
        }

        private @NotNull MessageDispatchInterceptor<EventMessage<?>> interceptor(String interceptorName) {
            return messages -> (index, event) -> {
                logger.debug(interceptorName + " logs event");
                return event;
            };
        }

    }

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> JavaArchiveTest.javaArchiveBase()
                    .addClasses(InterceptorsProducer.class, LoggerProducer.class));

    @Override
    protected void assertOthers() {
        InOrder inOrder = Mockito.inOrder(LOGGER);
        inOrder.verify(LOGGER).debug("Interceptor 1 logs event");
        inOrder.verify(LOGGER).debug("Interceptor 2 logs event");
        inOrder.verify(LOGGER).debug("Interceptor 1 logs event");
        inOrder.verify(LOGGER).debug("Interceptor 2 logs event");
    }
}
