package at.meks.quarkiverse.axon.eventprocessors.shared;

public interface StreamingProcessorConf extends EventProcessorConfiguration {

    /**
     * The initial position of the processor.
     */
    InitialPosition initialPosition();

}
