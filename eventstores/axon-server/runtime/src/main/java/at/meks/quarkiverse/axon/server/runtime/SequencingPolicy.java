package at.meks.quarkiverse.axon.server.runtime;

public enum SequencingPolicy {

    PER_AGGREGATE("SequentialPerAggregatePolicy");

    private final String axonName;

    SequencingPolicy(String axonName) {
        this.axonName = axonName;
    }

    String axonName() {
        return axonName;
    }
}
