package at.meks.quarkiverse.axon.deployment.devui;

import java.util.List;

public class BuildInfoApi {

    record Aggregate(String className) {
    }

    record SagaEventHandler(String className) {
    }

    record BuildInfo(List<Aggregate> aggregates, List<SagaEventHandler> sagaEventHandler) {
    }

}
