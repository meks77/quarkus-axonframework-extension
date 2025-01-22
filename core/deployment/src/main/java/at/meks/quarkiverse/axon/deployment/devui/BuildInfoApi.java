package at.meks.quarkiverse.axon.deployment.devui;

import java.util.List;

public class BuildInfoApi {

    record Aggregate(String className) {
    }

    record BuildInfo(List<Aggregate> aggregates) {
    }

}
