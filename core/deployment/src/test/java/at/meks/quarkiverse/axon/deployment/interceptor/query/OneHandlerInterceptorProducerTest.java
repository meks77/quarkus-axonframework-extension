package at.meks.quarkiverse.axon.deployment.interceptor.query;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.queryhandling.QueryMessage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.slf4j.Logger;

import at.meks.quarkiverse.axon.runtime.customizations.QueryHandlerInterceptorsProducer;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class OneHandlerInterceptorProducerTest extends JavaArchiveTest {

    private static final Logger LOGGER = Mockito.mock(Logger.class);

    public static class LoggerProducer {

        @Produces
        public Logger logger() {
            return LOGGER;
        }
    }

    @ApplicationScoped
    public static class InterceptorsProducer implements QueryHandlerInterceptorsProducer {

        private final Logger logger;

        public InterceptorsProducer(Logger logger) {
            this.logger = logger;
        }

        @Override
        public List<MessageHandlerInterceptor<QueryMessage<?, ?>>> createHandlerInterceptor() {
            return List.of(interceptor("Interceptor 1"), interceptor("Interceptor 2"));
        }

        private @NotNull MessageHandlerInterceptor<QueryMessage<?, ?>> interceptor(String interceptorName) {
            return (unitOfWork, interceptorChain) -> {
                logger.debug(interceptorName + " logs query");
                return interceptorChain.proceed();
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
        inOrder.verify(LOGGER).debug("Interceptor 1 logs query");
        inOrder.verify(LOGGER).debug("Interceptor 2 logs query");
    }
}
