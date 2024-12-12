package at.meks.quarkiverse.axon.deployment.interceptor.event;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.runtime.customizations.EventDispatchInterceptorsProducer;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class MoreDispatchInterceptorProducersTest extends JavaArchiveTest {

    @ApplicationScoped
    public static class InterceptorsProducer1 implements EventDispatchInterceptorsProducer {

        @Override
        public List<MessageDispatchInterceptor<EventMessage<?>>> createDispatchInterceptor() {
            return List.of(interceptor());
        }

    }

    @ApplicationScoped
    public static class InterceptorsProducer2 implements EventDispatchInterceptorsProducer {

        @Override
        public List<MessageDispatchInterceptor<EventMessage<?>>> createDispatchInterceptor() {
            return List.of(interceptor());
        }

    }

    private static @NotNull MessageDispatchInterceptor<EventMessage<?>> interceptor() {
        return messages -> (index, eventMessage) -> eventMessage;
    }

    @RegisterExtension()
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setExpectedException(IllegalStateException.class, true)
            .setArchiveProducer(() -> JavaArchiveTest.javaArchiveBase()
                    .addClasses(InterceptorsProducer1.class, InterceptorsProducer2.class));

}
