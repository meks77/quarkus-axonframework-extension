package at.meks.quarkiverse.axon.deployment;

import java.util.List;

import org.axonframework.eventsourcing.eventstore.AbstractEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.nativeimage.*;

public class NativeCompilationProcessor {

    @BuildStep
    ReflectiveClassBuildItem registerReflection() {
        return ReflectiveClassBuildItem.builder(JdbcEventStorageEngine.class, AbstractEventStorageEngine.class)
                .constructors(true).methods(true).fields(true).build();
    }

    @BuildStep
    List<RuntimeInitializedClassBuildItem> runtimeInitializeClasses() {
        return List.of(
                new RuntimeInitializedClassBuildItem(
                        "org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine"),
                new RuntimeInitializedClassBuildItem(
                        "org.axonframework.eventsourcing.eventstore.AbstractEventStorageEngine"),
                new RuntimeInitializedClassBuildItem(
                        "org.axonframework.eventsourcing.eventstore.AbstractEventStorageEngine$Builder"),
                new RuntimeInitializedClassBuildItem(
                        "org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine$Builder"),
                new RuntimeInitializedClassBuildItem("org.axonframework.serialization.xml.XStreamSerializer"));
    }

    @BuildStep
    ReflectiveHierarchyBuildItem axonStorageEngineReflectiveHierarchy() {
        return ReflectiveHierarchyBuildItem.builder(AbstractEventStorageEngine.Builder.class)
                .methods(true)
                .fields(true)
                .constructors(true)
                .build();
    }

    @BuildStep
    NativeImageSystemPropertyBuildItem enableSvmMethodHandles() {
        // Enable JDK MethodHandle support in native-image explicitly.
        return new NativeImageSystemPropertyBuildItem("svm.methodhandles", "enable");
    }

}
