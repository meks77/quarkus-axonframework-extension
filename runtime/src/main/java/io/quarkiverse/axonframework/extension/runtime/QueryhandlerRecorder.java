package io.quarkiverse.axonframework.extension.runtime;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.logging.Log;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class QueryhandlerRecorder {

    public void addQueryhandler(BeanContainer beanContainer, Class<?> queryhandlerClass) {
        Log.infof("Adding queryhandlerbean %s", queryhandlerClass);
        beanContainer.beanInstance(AxonExtension.class)
                .addQueryHandlerForRegistration(beanContainer.beanInstance(queryhandlerClass));
    }

}
