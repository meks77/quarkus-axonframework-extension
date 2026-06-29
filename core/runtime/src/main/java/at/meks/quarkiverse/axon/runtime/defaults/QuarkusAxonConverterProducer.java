package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.conversion.Converter;
import org.axonframework.conversion.jackson2.Jackson2Converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.customizations.AxonConverterProducer;
import io.quarkus.arc.DefaultBean;

@DefaultBean
@ApplicationScoped
public class QuarkusAxonConverterProducer implements AxonConverterProducer {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    AxonConfiguration axonConfiguration;

    private Jackson2Converter converter;

    @PostConstruct
    void init() {
        converter = new Jackson2Converter(objectMapper());
    }

    private ObjectMapper objectMapper() {
        if (!axonConfiguration.serialization().blackbird().enabled()) {
            return objectMapper;
        }
        return objectMapper.copy().registerModule(new BlackbirdModule());
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
