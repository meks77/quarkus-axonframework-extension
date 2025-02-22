package at.meks.quarkiverse.axon.deployment.commandhandler.duplicatecommandresolver;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.lifecycle.LifecycleHandlerInvocationException;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.model.Api;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class RejectDuplicatesTest extends JavaArchiveTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setExpectedException(LifecycleHandlerInvocationException.class)
            .setArchiveProducer(() -> JavaArchiveTest.javaArchiveBase()
                    .addClass(MyCommandHandler.class)
                    .addAsResource(propertiesFile("/commandhandler/duplicateResolver/rejectDuplicates.properties"),
                            "application.properties"));

    @ApplicationScoped
    public static class MyCommandHandler {

        @CommandHandler
        public void handle(Api.IssueCardCommand command) {

        }
    }

}
