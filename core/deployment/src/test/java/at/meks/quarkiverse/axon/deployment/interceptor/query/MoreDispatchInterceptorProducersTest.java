package at.meks.quarkiverse.axon.deployment.interceptor.query;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.queryhandling.QueryMessage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.runtime.customizations.QueryDispatchInterceptorsProducer;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class MoreDispatchInterceptorProducersTest extends JavaArchiveTest {

    @ApplicationScoped
    public static class InterceptorsProducer1 implements QueryDispatchInterceptorsProducer {

        @Override
        public List<MessageDispatchInterceptor<QueryMessage<?, ?>>> createDispatchInterceptor() {
            return List.of(interceptor());
        }

    }

    @ApplicationScoped
    public static class InterceptorsProducer2 implements QueryDispatchInterceptorsProducer {

        @Override
        public List<MessageDispatchInterceptor<QueryMessage<?, ?>>> createDispatchInterceptor() {
            return List.of(interceptor());
        }

    }

    private static @NotNull MessageDispatchInterceptor<QueryMessage<?, ?>> interceptor() {
        return messages -> (index, query) -> query;
    }

    @RegisterExtension()
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setExpectedException(IllegalStateException.class, true)
            .setArchiveProducer(() -> JavaArchiveTest.javaArchiveBase()
                    .addClasses(InterceptorsProducer1.class, InterceptorsProducer2.class));

}