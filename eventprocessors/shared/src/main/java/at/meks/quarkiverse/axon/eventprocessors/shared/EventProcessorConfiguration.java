package at.meks.quarkiverse.axon.eventprocessors.shared;

public interface EventProcessorConfiguration {

    int batchSize();

    int initialSegments();

}
