package io.quarkiverse.axonframework.extension.test.live.reloading;

import static org.assertj.core.api.Assertions.assertThatException;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.axonframework.extension.test.AbstractConfigurationTest;
import io.quarkiverse.axonframework.extension.test.model.Api;
import io.quarkus.test.QuarkusDevModeTest;
import io.restassured.RestAssured;

public class LiveReloadTest {

    @RegisterExtension
    final static QuarkusDevModeTest test = new QuarkusDevModeTest()
            .setArchiveProducer(() -> AbstractConfigurationTest.javaArchiveBase()
                    .addClasses(ConfigResource.class)
                    .addAsResource(AbstractConfigurationTest.propertiesFile("/live/reloading/application.properties"),
                            "application.properties"));

    /**
     * Repeated tests because reloading causes a timing issue in the axon framework or the axon server,
     * which doesn't occur each time. Before suspending the shutdown process, ~50 % of live reloads failed.
     */
    @RepeatedTest(4)
    public void testCommandHandlerChange() {
        String grpcPort = RestAssured.given().accept("text/plain").when().get(
                "/config/axonserverPort").then().statusCode(200).extract().body().asString();

        Configuration axonConfiguration = new TestAxonFrameworkConfigurer().configure(grpcPort).start();
        String cardId = UUID.randomUUID().toString();
        CommandGateway commandGateway = axonConfiguration.commandGateway();
        commandGateway.sendAndWait(new Api.IssueCardCommand(cardId, 10));
        commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId, 1));

        test.modifySourceFile("io/quarkiverse/axonframework/extension/test/model/DomainServiceExample.java",
                source -> source.replace(
                        "giftcardAggregate.execute(giftcard -> giftcard.requestRedeem(command.amount()));",
                        "throw new java.lang.IllegalStateException(\"whatever\");"));

        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThatException()
                        .isThrownBy(() -> commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId, 2)))
                        .havingCause()
                        .withMessageContaining("whatever"));
    }

}
