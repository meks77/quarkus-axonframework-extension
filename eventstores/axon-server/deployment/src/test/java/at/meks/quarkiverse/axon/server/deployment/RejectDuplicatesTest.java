package at.meks.quarkiverse.axon.server.deployment;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.lifecycle.LifecycleHandlerInvocationException;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.model.Api;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

public class RejectDuplicatesTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = application(javaArchiveBase().addClass(MyCommandHandler.class))
            .setExpectedException(LifecycleHandlerInvocationException.class)
            .withConfigurationResource("rejectDuplicates.properties");

    @ApplicationScoped
    public static class MyCommandHandler {

        @CommandHandler
        public void handle(Api.IssueCardCommand command) {

        }
    }

}
