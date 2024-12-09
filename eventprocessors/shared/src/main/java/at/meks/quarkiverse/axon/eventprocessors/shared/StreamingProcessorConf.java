package at.meks.quarkiverse.axon.eventprocessors.shared;

public interface StreamingProcessorConf extends EventProcessorConfiguration {

    InitialPosition initialPosition();

}
