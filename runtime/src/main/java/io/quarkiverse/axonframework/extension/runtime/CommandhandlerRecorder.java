package io.quarkiverse.axonframework.extension.runtime;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.logging.Log;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class CommandhandlerRecorder {

    public void addCommandhandler(BeanContainer beanContainer, Class<?> commandhandlerClass) {
        Log.infof("Adding commandhandlerbean %s", commandhandlerClass);
        beanContainer.beanInstance(AxonExtension.class)
                .addCommandhandlerForRegistration(beanContainer.beanInstance(commandhandlerClass));
    }

}
