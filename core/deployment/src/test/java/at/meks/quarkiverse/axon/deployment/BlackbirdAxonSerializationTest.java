package at.meks.quarkiverse.axon.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.axonframework.conversion.Converter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.JsonNode;

import at.meks.quarkiverse.axon.runtime.customizations.AxonConverterProducer;
import at.meks.quarkiverse.axon.shared.model.Api;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

public class BlackbirdAxonSerializationTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .setArchiveProducer(JavaArchiveTest::javaArchiveBase)
            .withConfigurationResource("serialization/blackbird.properties");

    @Inject
    AxonConverterProducer converterProducer;

    @Test
    void axonConvertersRoundTripPayloadsWithBlackbirdEnabled() {
        assertRoundTrip(converterProducer.createEventConverter(), new Api.CardIssuedEvent("card-1", 10));
        assertRoundTrip(converterProducer.createMessageConverter(), new Api.IssueCardCommand("card-1", 10));
        assertRoundTrip(converterProducer.createGeneralConverter(), new SnapshotPayload("card-1", 10));
    }

    private static <T> void assertRoundTrip(Converter converter, T value) {
        JsonNode serialized = converter.convert(value, JsonNode.class);
        Object deserialized = converter.convert(serialized, value.getClass());

        assertThat(deserialized).isEqualTo(value);
    }

    public record SnapshotPayload(String cardId, int currentAmount) {
    }

}
