package io.quarkiverse.axonframework.extension.runtime;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.logging.Log;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class AggregateRecorder {

    public void addAggregate(BeanContainer beanContainer, Class<?> aggregateClass) {
        Log.infof("Adding aggregate %s", aggregateClass);
        beanContainer.beanInstance(AxonExtension.class).registerAggregate(aggregateClass);
    }

}
