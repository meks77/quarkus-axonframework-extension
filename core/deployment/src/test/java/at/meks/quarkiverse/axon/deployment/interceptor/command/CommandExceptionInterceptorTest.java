package at.meks.quarkiverse.axon.deployment.interceptor.command;

import static org.assertj.core.api.Assertions.assertThatException;

import java.util.UUID;

import jakarta.inject.Inject;

import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.model.Api;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class CommandExceptionInterceptorTest {

    @Inject
    CommandGateway commandGateway;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(JavaArchiveTest::javaArchiveBase);

    @Test
    void onExceptionWhenCommandIsHandled() {
        var cardId = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new Api.IssueCardCommand(cardId, 10));
        commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId, 1));
        assertThatException()
                .isThrownBy(() -> commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId, 10)))
                .isInstanceOf(CommandExecutionException.class)
                .withMessageContaining(
                        "error while executing command handler for command at.meks.quarkiverse.axon.shared.model.Api$RedeemCardCommand")
                .withStackTraceContaining("amount must be less than current card amount");
    }
}
