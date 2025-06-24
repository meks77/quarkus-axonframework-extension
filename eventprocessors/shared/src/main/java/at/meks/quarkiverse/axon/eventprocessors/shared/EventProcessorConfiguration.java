package at.meks.quarkiverse.axon.eventprocessors.shared;

import java.util.Optional;

public interface EventProcessorConfiguration {

    Optional<Integer> batchSize();

    Optional<Integer> initialSegments();

}
