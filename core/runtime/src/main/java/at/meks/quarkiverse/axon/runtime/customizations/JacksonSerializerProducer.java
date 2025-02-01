package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.serialization.json.JacksonSerializer;

public interface JacksonSerializerProducer {

    JacksonSerializer createSerializer();

}
