package at.meks.quarkiverse.axon.deployment.interceptor.command;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.messaging.commandhandling.CommandMessage;
import org.axonframework.messaging.core.MessageDispatchInterceptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.runtime.customizations.CommandDispatchInterceptorsProducer;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

public class MoreDispatchInterceptorProducersTest extends JavaArchiveTest {

    @ApplicationScoped
    public static class InterceptorsProducer1 implements CommandDispatchInterceptorsProducer {

        @Override
        public List<MessageDispatchInterceptor<CommandMessage>> createDispatchInterceptor() {
            return List.of(interceptor());
        }

    }

    @ApplicationScoped
    public static class InterceptorsProducer2 implements CommandDispatchInterceptorsProducer {

        @Override
        public List<MessageDispatchInterceptor<CommandMessage>> createDispatchInterceptor() {
            return List.of(interceptor());
        }

    }

    private static @NotNull MessageDispatchInterceptor<CommandMessage> interceptor() {
        return (message, context, interceptorChain) -> interceptorChain.proceed(message, context);
    }

    @RegisterExtension()
    static final QuarkusExtensionTest config = application(javaArchiveBase()
            .addClasses(InterceptorsProducer1.class, InterceptorsProducer2.class))
            .setExpectedException(IllegalStateException.class, true);

}
