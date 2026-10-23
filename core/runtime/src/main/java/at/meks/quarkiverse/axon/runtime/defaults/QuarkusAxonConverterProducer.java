package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Reserve;
import jakarta.inject.Inject;

import org.axonframework.conversion.Converter;
import org.axonframework.conversion.jackson.JacksonConverter;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.customizations.AxonConverterProducer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.module.blackbird.BlackbirdModule;

@Reserve
@Priority(0)
@ApplicationScoped
public class QuarkusAxonConverterProducer implements AxonConverterProducer {

    @Inject
    JsonMapper objectMapper;

    @Inject
    AxonConfiguration axonConfiguration;

    private JacksonConverter converter;

    @PostConstruct
    void init() {
        converter = new JacksonConverter(objectMapper());
    }

    private JsonMapper objectMapper() {
        if (!axonConfiguration.serialization().blackbird().enabled()) {
            return objectMapper;
        }
        return objectMapper.rebuild()
                .addModule(new BlackbirdModule())
                .build();
    }

    @Override
    public Converter createGeneralConverter() {
        return converter;
    }

    @Override
    public Converter createEventConverter() {
        return converter;
    }

    @Override
    public Converter createMessageConverter() {
        return converter;
    }

}
