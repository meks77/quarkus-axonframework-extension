package at.meks.quarkiverse.axon.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.conversion.Converter;
import org.axonframework.conversion.jackson2.Jackson2Converter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.meks.quarkiverse.axon.runtime.customizations.AxonConverterProducer;
import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.test.QuarkusExtensionTest;

public class CustomAxonConverterProducerTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .setArchiveProducer(() -> JavaArchiveTest.javaArchiveBase().addClasses(CustomConverterProducer.class))
            .withConfigurationResource("serialization/blackbird.properties");

    @Inject
    AxonConverterProducer axonConverterProducer;

    @Test
    void customAxonConverterProducerWinsWhenBlackbirdIsEnabled() throws Exception {
        assertThat(axonConverterProducer).isInstanceOf(CustomConverterProducer.class);

        assertThat(axonConverterProducer.createGeneralConverter()).isSameAs(CustomConverterProducer.generalConverter);
        assertThat(axonConverterProducer.createEventConverter()).isSameAs(CustomConverterProducer.eventConverter);
        assertThat(axonConverterProducer.createMessageConverter()).isSameAs(CustomConverterProducer.messageConverter);

        assertThat(hasBlackbirdModule(CustomConverterProducer.generalConverter)).isFalse();
        assertThat(hasBlackbirdModule(CustomConverterProducer.eventConverter)).isFalse();
        assertThat(hasBlackbirdModule(CustomConverterProducer.messageConverter)).isFalse();
    }

    private static boolean hasBlackbirdModule(Converter converter) throws Exception {
        Field objectMapper = Jackson2Converter.class.getDeclaredField("objectMapper");
        objectMapper.setAccessible(true);
        ObjectMapper mapper = (ObjectMapper) objectMapper.get(converter);
        return mapper.getRegisteredModuleIds().stream()
                .map(Object::toString)
                .anyMatch(moduleId -> moduleId.toLowerCase().contains("blackbird"));
    }

    @ApplicationScoped
    public static class CustomConverterProducer implements AxonConverterProducer {

        static final Jackson2Converter generalConverter = converter();
        static final Jackson2Converter eventConverter = converter();
        static final Jackson2Converter messageConverter = converter();

        @Override
        public Converter createGeneralConverter() {
            return generalConverter;
        }

        @Override
        public Converter createEventConverter() {
            return eventConverter;
        }

        @Override
        public Converter createMessageConverter() {
            return messageConverter;
        }

        private static Jackson2Converter converter() {
            return new Jackson2Converter(new ObjectMapper());
        }

    }

}
