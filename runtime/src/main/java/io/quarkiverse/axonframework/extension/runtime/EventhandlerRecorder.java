package io.quarkiverse.axonframework.extension.runtime;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.logging.Log;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class EventhandlerRecorder {

    public void addEventhandler(BeanContainer beanContainer, Class<?> eventhandlerClass) {
        Log.infof("Adding eventhandlerbean %s", eventhandlerClass);
        beanContainer.beanInstance(AxonExtension.class)
                .addEventhandlerForRegistration(beanContainer.beanInstance(eventhandlerClass));
    }

}
