package at.meks.quarkiverse.axon.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.config.Configuration;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.meks.quarkiverse.axon.runtime.customizations.AxonSerializerProducer;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusUnitTest;

public class CustomAxonSerializerProducerTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> JavaArchiveTest.javaArchiveBase().addClasses(CustomSerializerProducer.class))
            .withConfigurationResource("serialization/blackbird.properties");

    @Inject
    AxonSerializerProducer axonSerializerProducer;

    @Inject
    Configuration configuration;

    @Test
    void customAxonSerializerProducerWinsWhenBlackbirdIsEnabled() {
        assertThat(axonSerializerProducer).isInstanceOf(CustomSerializerProducer.class);

        assertThat(configuration.serializer()).isSameAs(CustomSerializerProducer.serializer);
        assertThat(configuration.eventSerializer()).isSameAs(CustomSerializerProducer.eventSerializer);
        assertThat(configuration.messageSerializer()).isSameAs(CustomSerializerProducer.messageSerializer);

        assertThat(hasBlackbirdModule(CustomSerializerProducer.serializer)).isFalse();
        assertThat(hasBlackbirdModule(CustomSerializerProducer.eventSerializer)).isFalse();
        assertThat(hasBlackbirdModule(CustomSerializerProducer.messageSerializer)).isFalse();
    }

    private static boolean hasBlackbirdModule(Serializer serializer) {
        ObjectMapper mapper = ((JacksonSerializer) serializer).getObjectMapper();
        return mapper.getRegisteredModuleIds().stream()
                .map(Object::toString)
                .anyMatch(moduleId -> moduleId.toLowerCase().contains("blackbird"));
    }

    @ApplicationScoped
    public static class CustomSerializerProducer implements AxonSerializerProducer {

        static final JacksonSerializer serializer = serializer();
        static final JacksonSerializer eventSerializer = serializer();
        static final JacksonSerializer messageSerializer = serializer();

        @Override
        public Serializer createSerializer() {
            return serializer;
        }

        @Override
        public Serializer createEventSerializer() {
            return eventSerializer;
        }

        @Override
        public Serializer createMessageSerializer() {
            return messageSerializer;
        }

        private static JacksonSerializer serializer() {
            return JacksonSerializer.builder()
                    .objectMapper(new ObjectMapper())
                    .build();
        }

    }

}
