package at.meks.quarkiverse.axon.deployment.interceptor.event;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.axonframework.messaging.core.MessageDispatchInterceptor;
import org.axonframework.messaging.eventhandling.EventMessage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.slf4j.Logger;

import at.meks.quarkiverse.axon.runtime.customizations.EventDispatchInterceptorsProducer;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

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
        public List<MessageDispatchInterceptor<EventMessage>> createDispatchInterceptor() {
            return List.of(interceptor("Interceptor 1"), interceptor("Interceptor 2"));
        }

        private @NotNull MessageDispatchInterceptor<EventMessage> interceptor(String interceptorName) {
            return ((message, context, interceptorChain) -> {
                logger.debug("{} logs event", interceptorName);
                return interceptorChain.proceed(message, context);
            });
        }

    }

    @RegisterExtension
    static final QuarkusExtensionTest config = application(javaArchiveBase()
            .addClasses(InterceptorsProducer.class, LoggerProducer.class));

    @Override
    protected void assertOthers() {
        InOrder inOrder = Mockito.inOrder(LOGGER);
        inOrder.verify(LOGGER).debug("{} logs event", "Interceptor 1");
        inOrder.verify(LOGGER).debug("{} logs event", "Interceptor 2");
        inOrder.verify(LOGGER).debug("{} logs event", "Interceptor 1");
        inOrder.verify(LOGGER).debug("{} logs event", "Interceptor 2");
    }
}
