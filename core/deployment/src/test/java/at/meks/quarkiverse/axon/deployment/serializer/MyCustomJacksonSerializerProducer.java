package at.meks.quarkiverse.axon.deployment.serializer;

import jakarta.enterprise.context.ApplicationScoped;

import org.axonframework.serialization.json.JacksonSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import at.meks.quarkiverse.axon.runtime.customizations.JacksonSerializerProducer;
import io.quarkus.logging.Log;

@ApplicationScoped
public class MyCustomJacksonSerializerProducer implements JacksonSerializerProducer {

    public static JacksonSerializer INSTANCE = JacksonSerializer.builder().objectMapper(createObjectMapper()).build();
    private static boolean customSerializerUsed;

    @Override
    public JacksonSerializer createSerializer() {
        Log.info("My custom Jackson serializer producer is used.");
        customSerializerUsed = true;
        return INSTANCE;
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.getVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY);
        return objectMapper;
    }

    public static boolean isCustomSerializerUsed() {
        return customSerializerUsed;
    }
}
