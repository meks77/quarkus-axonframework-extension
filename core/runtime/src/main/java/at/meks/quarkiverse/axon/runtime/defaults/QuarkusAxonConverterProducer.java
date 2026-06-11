package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.conversion.Converter;
import org.axonframework.conversion.jackson2.Jackson2Converter;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.meks.quarkiverse.axon.runtime.customizations.AxonConverterProducer;
import io.quarkus.arc.DefaultBean;

@DefaultBean
@ApplicationScoped
public class QuarkusAxonConverterProducer implements AxonConverterProducer {

    @Inject
    ObjectMapper objectMapper;

    private Jackson2Converter serializer;

    @PostConstruct
    void init() {
        serializer = new Jackson2Converter(objectMapper);
    }

    @Override
    public Converter createGeneralConverter() {
        return serializer;
    }

    @Override
    public Converter createEventConverter() {
        return serializer;
    }

    @Override
    public Converter createMessageConverter() {
        return serializer;
    }

}
