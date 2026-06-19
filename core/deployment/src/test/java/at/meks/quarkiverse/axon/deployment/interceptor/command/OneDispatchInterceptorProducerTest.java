package at.meks.quarkiverse.axon.deployment.interceptor.command;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.axonframework.messaging.commandhandling.CommandMessage;
import org.axonframework.messaging.core.MessageDispatchInterceptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.slf4j.Logger;

import at.meks.quarkiverse.axon.runtime.customizations.CommandDispatchInterceptorsProducer;
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
    public static class InterceptorsProducer implements CommandDispatchInterceptorsProducer {

        private final Logger logger;

        public InterceptorsProducer(Logger logger) {
            this.logger = logger;
        }

        @Override
        public List<MessageDispatchInterceptor<CommandMessage>> createDispatchInterceptor() {
            return List.of(interceptor("Interceptor 1"), interceptor("Interceptor 2"));
        }

        private @NotNull MessageDispatchInterceptor<CommandMessage> interceptor(String interceptorName) {
            return (message, context, interceptorChain) -> {
                logger.debug("{} logs command", interceptorName);
                return interceptorChain.proceed(message, context);
            };
        }

    }

    @RegisterExtension
    static final QuarkusExtensionTest config = application(javaArchiveBase()
            .addClasses(InterceptorsProducer.class, LoggerProducer.class));

    @Override
    protected void assertOthers() {
        InOrder inOrder = Mockito.inOrder(LOGGER);
        inOrder.verify(LOGGER).debug("{} logs command", "Interceptor 1");
        inOrder.verify(LOGGER).debug("{} logs command", "Interceptor 2");
        inOrder.verify(LOGGER).debug("{} logs command", "Interceptor 1");
        inOrder.verify(LOGGER).debug("{} logs command", "Interceptor 2");
    }
}
