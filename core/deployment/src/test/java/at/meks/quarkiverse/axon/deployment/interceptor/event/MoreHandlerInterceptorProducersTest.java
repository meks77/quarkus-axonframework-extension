package at.meks.quarkiverse.axon.deployment.interceptor.event;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.runtime.customizations.EventHandlerInterceptorsProducer;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class MoreHandlerInterceptorProducersTest extends JavaArchiveTest {

    @ApplicationScoped
    public static class InterceptorsProducer1 implements EventHandlerInterceptorsProducer {

        @Override
        public List<MessageHandlerInterceptor<EventMessage<?>>> createHandlerInterceptor() {
            return List.of(interceptor());
        }

    }

    @ApplicationScoped
    public static class InterceptorsProducer2 implements EventHandlerInterceptorsProducer {

        @Override
        public List<MessageHandlerInterceptor<EventMessage<?>>> createHandlerInterceptor() {
            return List.of(interceptor());
        }

    }

    private static @NotNull MessageHandlerInterceptor<EventMessage<?>> interceptor() {
        return (unitOfWork, interceptorChain) -> interceptorChain.proceed();
    }

    @RegisterExtension()
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setExpectedException(IllegalStateException.class, true)
            .setArchiveProducer(() -> JavaArchiveTest.javaArchiveBase()
                    .addClasses(InterceptorsProducer1.class, InterceptorsProducer2.class));

}
