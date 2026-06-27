package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;

import at.meks.quarkiverse.axon.runtime.conf.AxonConfiguration;
import at.meks.quarkiverse.axon.runtime.customizations.AxonSerializerProducer;
import io.quarkus.arc.DefaultBean;

@DefaultBean
@ApplicationScoped
public class QuarkusAxonSerializerProducer implements AxonSerializerProducer {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    AxonConfiguration axonConfiguration;

    private JacksonSerializer serializer;

    @PostConstruct
    void init() {
        serializer = JacksonSerializer.builder().objectMapper(objectMapper()).build();
    }

    private ObjectMapper objectMapper() {
        if (!axonConfiguration.serialization().blackbird().enabled()) {
            return objectMapper;
        }
        return objectMapper.copy().registerModule(new BlackbirdModule());
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
