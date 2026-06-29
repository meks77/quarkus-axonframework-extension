package at.meks.quarkiverse.axon.runtime.defaults;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.axonframework.conversion.Converter;
import org.axonframework.conversion.jackson2.Jackson2Converter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.shared.model.Api;

class QuarkusAxonConverterProducerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AxonConfiguration axonConfiguration = mock(AxonConfiguration.class);
    private final AxonConfiguration.SerializationConfig serializationConfig = mock(AxonConfiguration.SerializationConfig.class);
    private final AxonConfiguration.BlackbirdConfig blackbirdConfig = mock(AxonConfiguration.BlackbirdConfig.class);

    private QuarkusAxonConverterProducer producer;

    @BeforeEach
    void setUp() {
        when(axonConfiguration.serialization()).thenReturn(serializationConfig);
        when(serializationConfig.blackbird()).thenReturn(blackbirdConfig);

        producer = new QuarkusAxonConverterProducer();
        producer.objectMapper = objectMapper;
        producer.axonConfiguration = axonConfiguration;
    }

    @Test
    void defaultConvertersRoundTripWithoutBlackbird() throws Exception {
        when(blackbirdConfig.enabled()).thenReturn(false);

        producer.init();

        assertRoundTrip(producer.createGeneralConverter(), new Api.CardIssuedEvent("card-1", 10));
        assertRoundTrip(producer.createEventConverter(), new Api.CardRedeemedEvent("card-1", 3));
        assertRoundTrip(producer.createMessageConverter(), new Api.IssueCardCommand("card-1", 10));
        assertThat(objectMapper(producer.createGeneralConverter())).isSameAs(objectMapper);
    }

    @Test
    void blackbirdUsesAxonSpecificObjectMapperCopy() throws Exception {
        when(blackbirdConfig.enabled()).thenReturn(true);

        producer.init();

        ObjectMapper axonObjectMapper = objectMapper(producer.createGeneralConverter());
        assertThat(axonObjectMapper).isNotSameAs(objectMapper);
        assertThat(hasBlackbirdModule(axonObjectMapper)).isTrue();
        assertThat(hasBlackbirdModule(objectMapper)).isFalse();
        assertRoundTrip(producer.createGeneralConverter(), new Api.CardIssuedEvent("card-1", 10));
    }

    private static <T> void assertRoundTrip(Converter converter, T value) {
        JsonNode json = converter.convert(value, JsonNode.class);
        Object deserialized = converter.convert(json, value.getClass());

        assertThat(deserialized).isEqualTo(value);
    }

    private static ObjectMapper objectMapper(Converter converter) throws Exception {
        Field objectMapper = Jackson2Converter.class.getDeclaredField("objectMapper");
        objectMapper.setAccessible(true);
        return (ObjectMapper) objectMapper.get(converter);
    }

    private static boolean hasBlackbirdModule(ObjectMapper mapper) {
        return mapper.getRegisteredModuleIds().stream()
                .map(Object::toString)
                .anyMatch(moduleId -> moduleId.toLowerCase().contains("blackbird"));
    }

}
