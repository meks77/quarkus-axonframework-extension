package at.meks.quarkiverse.axon.runtime.defaults;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.serialization.json.JacksonSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.meks.quarkiverse.axon.runtime.customizations.JacksonSerializerProducer;
import io.quarkus.arc.DefaultBean;
import io.quarkus.logging.Log;

@DefaultBean
@ApplicationScoped
public class QuarkusJacksonSerializerProducer implements JacksonSerializerProducer {

    @Inject
    ObjectMapper objectMapper;

    @Override
    public JacksonSerializer createSerializer() {
        Log.info("Quarkus objectMapper is used for serialization");
        return JacksonSerializer.builder().objectMapper(objectMapper).build();
    }

}
