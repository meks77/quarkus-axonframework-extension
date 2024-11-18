package at.meks.quarkiverse.axon.deployment.live.reloading;

import static org.assertj.core.api.Assertions.assertThatException;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.deployment.streamingprocessors.pooled.PooledProcessorTest;
import at.meks.quarkiverse.axon.shared.model.Api;
import at.meks.quarkiverse.axon.shared.model.Giftcard;
import io.quarkus.test.QuarkusDevModeTest;
import io.restassured.RestAssured;

public class LiveReloadTest {

    @RegisterExtension
    final static QuarkusDevModeTest test = new QuarkusDevModeTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(ConfigResource.class, DomainServiceForLiveReloading.class, Api.class, Giftcard.class)
                    .addAsResource(PooledProcessorTest.propertiesFile("/live/reloading/application.properties"),
                            "application.properties"));

    /**
     * Repeated tests because reloading causes a timing issue in the axon framework or the axon server,
     * which doesn't occur each time. Before suspending the shutdown process, ~50 % of live reloads failed.
     * On a MacBook Pro M1 it works(90 of 90 attempts) with a wait of 500ms after shutting down the axon configuration.
     * In the build with github actions, even 10 seconds didn't help
     */
    @RepeatedTest(4)
    @Tag("live-reload")
    public void testCommandHandlerChange() {
        String grpcPort = RestAssured.given().accept("text/plain").when().get(
                "/config/axonserverPort").then().statusCode(200).extract().body().asString();

        Configuration axonConfiguration = new TestAxonFrameworkConfigurer().configure(grpcPort).start();
        String cardId = UUID.randomUUID().toString();
        CommandGateway commandGateway = axonConfiguration.commandGateway();
        commandGateway.sendAndWait(new Api.IssueCardCommand(cardId, 10));
        commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId, 1));

        test.modifySourceFile("at/meks/quarkiverse/axon/deployment/live/reloading/DomainServiceForLiveReloading.java",
                source -> source.replace(
                        "giftcardAggregate.execute(giftcard -> giftcard.requestRedeem(command.amount()));",
                        "throw new java.lang.IllegalStateException(\"whatever\");"));

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThatException()
                        .isThrownBy(() -> commandGateway.sendAndWait(new Api.RedeemCardCommand(cardId, 2)))
                        .havingCause()
                        .withMessageContaining("whatever"));
    }

}
