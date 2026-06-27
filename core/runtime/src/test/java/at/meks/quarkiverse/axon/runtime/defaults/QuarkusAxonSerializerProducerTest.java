package at.meks.quarkiverse.axon.runtime.defaults;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.shared.model.Api;

class QuarkusAxonSerializerProducerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AxonConfiguration axonConfiguration = mock(AxonConfiguration.class);
    private final AxonConfiguration.SerializationConfig serializationConfig = mock(AxonConfiguration.SerializationConfig.class);
    private final AxonConfiguration.BlackbirdConfig blackbirdConfig = mock(AxonConfiguration.BlackbirdConfig.class);

    private QuarkusAxonSerializerProducer producer;

    @BeforeEach
    void setUp() {
        when(axonConfiguration.serialization()).thenReturn(serializationConfig);
        when(serializationConfig.blackbird()).thenReturn(blackbirdConfig);

        producer = new QuarkusAxonSerializerProducer();
        producer.objectMapper = objectMapper;
        producer.axonConfiguration = axonConfiguration;
    }

    @Test
    void defaultSerializerRoundTripsWithoutBlackbird() {
        when(blackbirdConfig.enabled()).thenReturn(false);

        producer.init();

        assertRoundTrip(producer.createSerializer(), new Api.CardIssuedEvent("card-1", 10));
        assertRoundTrip(producer.createEventSerializer(), new Api.CardRedeemedEvent("card-1", 3));
        assertRoundTrip(producer.createMessageSerializer(), new Api.IssueCardCommand("card-1", 10));
        assertThat(producer.createSerializer().getObjectMapper()).isSameAs(objectMapper);
    }

    @Test
    void blackbirdUsesAxonSpecificObjectMapperCopy() {
        when(blackbirdConfig.enabled()).thenReturn(true);

        producer.init();

        JacksonSerializer serializer = producer.createSerializer();
        assertThat(serializer.getObjectMapper()).isNotSameAs(objectMapper);
        assertThat(hasBlackbirdModule(serializer.getObjectMapper())).isTrue();
        assertThat(hasBlackbirdModule(objectMapper)).isFalse();
        assertRoundTrip(serializer, new Api.CardIssuedEvent("card-1", 10));
    }

    private <T> void assertRoundTrip(Serializer serializer, T value) {
        var serialized = serializer.serialize(value, String.class);
        Object deserialized = serializer.deserialize(serialized);

        assertThat(deserialized).isEqualTo(value);
    }

    private boolean hasBlackbirdModule(ObjectMapper mapper) {
        return mapper.getRegisteredModuleIds().stream()
                .map(Object::toString)
                .anyMatch(moduleId -> moduleId.toLowerCase().contains("blackbird"));
    }

}
