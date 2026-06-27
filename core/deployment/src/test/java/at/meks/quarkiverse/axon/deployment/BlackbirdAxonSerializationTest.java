package at.meks.quarkiverse.axon.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.axonframework.config.Configuration;
import org.axonframework.serialization.Serializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.model.Api;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class BlackbirdAxonSerializationTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(JavaArchiveTest::javaArchiveBase)
            .withConfigurationResource("serialization/blackbird.properties");

    @Inject
    Configuration configuration;

    @Test
    void axonSerializersRoundTripPayloadsWithBlackbirdEnabled() {
        assertRoundTrip(configuration.eventSerializer(), new Api.CardIssuedEvent("card-1", 10));
        assertRoundTrip(configuration.messageSerializer(), new Api.IssueCardCommand("card-1", 10));
        assertRoundTrip(configuration.serializer(), new SnapshotPayload("card-1", 10));
    }

    private static <T> void assertRoundTrip(Serializer serializer, T value) {
        var serialized = serializer.serialize(value, String.class);
        Object deserialized = serializer.deserialize(serialized);

        assertThat(deserialized).isEqualTo(value);
    }

    public record SnapshotPayload(String cardId, int currentAmount) {
    }

}
