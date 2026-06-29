package at.meks.quarkiverse.axon.runtime.customizations;

import org.axonframework.conversion.Converter;

public interface AxonConverterProducer {

    Converter createGeneralConverter();

    Converter createEventConverter();

    Converter createMessageConverter();

}
