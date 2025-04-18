package at.meks.quarkiverse.axon.deployment;

import java.util.List;

import org.axonframework.eventsourcing.eventstore.AbstractEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.nativeimage.*;

public class NativeCompilationProcessor {

    @BuildStep
    ReflectiveClassBuildItem registerReflection() {
        return ReflectiveClassBuildItem
                .builder(JdbcEventStorageEngine.class, AbstractEventStorageEngine.class,
                        JdbcEventStorageEngine.Builder.class,
                        AbstractEventStorageEngine.Builder.class)
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
                new RuntimeInitializedClassBuildItem("org.axonframework.serialization.xml.XStreamSerializer"),
                new RuntimeInitializedClassBuildItem(
                        "com.thoughtworks.xstream.converters.extended.DynamicProxyConverter$Reflections"),
                new RuntimeInitializedClassBuildItem("sun.java2d.Disposer"),
                new RuntimeInitializedClassBuildItem("sun.font.SunFontManager"),
                new RuntimeInitializedClassBuildItem("java.awt.Font"),
                new RuntimeInitializedClassBuildItem("sun.font.PhysicalStrike"),
                new RuntimeInitializedClassBuildItem("io.grpc.netty.shaded.io.netty.internal.tcnative.SSL"),
                new RuntimeInitializedClassBuildItem("io.grpc.netty.shaded.io.netty.internal.tcnative.SSLPrivateKeyMethod"),
                new RuntimeInitializedClassBuildItem(
                        "io.grpc.netty.shaded.io.netty.internal.tcnative.AsyncSSLPrivateKeyMethod"),
                new RuntimeInitializedClassBuildItem(
                        "io.grpc.netty.shaded.io.netty.internal.tcnative.CertificateCompressionAlgo"),
                new RuntimeInitializedClassBuildItem(
                        "io.grpc.netty.shaded.io.netty.handler.ssl.JdkNpnApplicationProtocolNegotiator"),
                new RuntimeInitializedClassBuildItem("io.grpc.netty.shaded.io.netty.internal.tcnative.CertificateVerifier"),
                new RuntimeInitializedClassBuildItem("io.grpc.netty.shaded.io.netty.handler.ssl.OpenSslAsyncPrivateKeyMethod"),
                new RuntimeInitializedClassBuildItem("io.grpc.netty.shaded.io.netty.handler.ssl.OpenSslPrivateKeyMethod"),
                new RuntimeInitializedClassBuildItem("io.grpc.internal.RetriableStream"),
                new RuntimeInitializedClassBuildItem("io.grpc.netty.shaded.io.netty.util.internal.logging.Log4J2Logger"),
                new RuntimeInitializedClassBuildItem("sun.font.StrikeCache"),
                new RuntimeInitializedClassBuildItem("io.netty.handler.codec.compression.ZstdConstants"));

    }

    @BuildStep
    List<RuntimeInitializedPackageBuildItem> runtimeInitializePackages() {
        return List.of(new RuntimeInitializedPackageBuildItem("com.thouthworks.xstream.converters"),
                new RuntimeInitializedPackageBuildItem("com.sun.font"),
                new RuntimeInitializedPackageBuildItem("sun.font"));
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
