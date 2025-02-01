package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.meks.quarkiverse.axon.runtime.customizations.AxonSerializerProducer;
import io.quarkus.arc.DefaultBean;

@DefaultBean
@ApplicationScoped
public class QuarkusAxonSerializerProducer implements AxonSerializerProducer {

    @Inject
    ObjectMapper objectMapper;

    private JacksonSerializer serializer;

    @PostConstruct
    void init() {
        serializer = JacksonSerializer.builder().objectMapper(objectMapper).build();
    }

    @Override
    public JacksonSerializer createSerializer() {
        return serializer;
    }

    @Override
    public Serializer createEventSerializer() {
        return serializer;
    }

    @Override
    public Serializer createMessageSerializer() {
        return serializer;
    }

}
