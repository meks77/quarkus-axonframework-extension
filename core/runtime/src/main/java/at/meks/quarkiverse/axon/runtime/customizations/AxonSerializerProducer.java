package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.serialization.Serializer;

public interface AxonSerializerProducer {

    Serializer createSerializer();

    Serializer createEventSerializer();

    Serializer createMessageSerializer();

}
