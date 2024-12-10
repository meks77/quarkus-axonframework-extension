package at.meks.quarkiverse.axon.deployment.interceptor.command;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.runtime.CommandDispatchInterceptorsProducer;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class MoreDispatchProducersTest extends JavaArchiveTest {

    @ApplicationScoped
    public static class InterceptorsProducer1 implements CommandDispatchInterceptorsProducer {

        @Override
        public List<MessageDispatchInterceptor<CommandMessage<?>>> createDispatchInterceptor() {
            return List.of(interceptor());
        }

    }

    @ApplicationScoped
    public static class InterceptorsProducer2 implements CommandDispatchInterceptorsProducer {

        @Override
        public List<MessageDispatchInterceptor<CommandMessage<?>>> createDispatchInterceptor() {
            return List.of(interceptor());
        }

    }

    private static @NotNull MessageDispatchInterceptor<CommandMessage<?>> interceptor() {
        return messages -> (index, command) -> command;
    }

    @RegisterExtension()
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setExpectedException(IllegalStateException.class, true)
            .setArchiveProducer(() -> JavaArchiveTest.javaArchiveBase()
                    .addClasses(InterceptorsProducer1.class, InterceptorsProducer2.class));

}
