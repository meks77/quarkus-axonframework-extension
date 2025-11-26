package at.meks.quarkiverse.axon.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.jboss.jandex.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import at.meks.quarkiverse.axon.runtime.conf.ComponentDiscoveryConfiguration;
import at.meks.quarkiverse.axon.runtime.conf.ComponentDiscoveryConfiguration.ComponentDiscovery;
import at.meks.quarkiverse.axon.shared.model.CardReturnSaga;
import at.meks.quarkiverse.axon.shared.model.DomainServiceExample;
import at.meks.quarkiverse.axon.shared.model.Giftcard;
import at.meks.quarkiverse.axon.shared.projection.GiftcardQueryHandler;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;

@ExtendWith(MockitoExtension.class)
class ClassDiscoveryTest {

    @Mock(strictness = Mock.Strictness.LENIENT)
    private ComponentDiscoveryConfiguration discoveryConfiguration;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private BeanArchiveIndexBuildItem beanArchiveIndex;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private IndexView indexView;

    @BeforeEach
    void setup() {
        when(beanArchiveIndex.getIndex()).thenReturn(indexView);
    }

    @Nested
    class Classes {

        @Mock
        ComponentDiscovery componentDiscovery;

        @ParameterizedTest
        @ValueSource(strings = { "aggregate", "eventhandler", "commandhandler", "queryhandler", "sagahandler" })
        void enabledDiscovery(String type) {
            when(componentDiscovery.enabled()).thenReturn(true);
            when(componentDiscovery.includedPackages()).thenReturn(Optional.empty());
            List<? extends ClassProvider> axonClassBuildItems = List.of(new AggregateBeanBuildItem(Giftcard.class));

            Set<Class<?>> result = ClassDiscovery.classes(axonClassBuildItems, type, componentDiscovery);

            assertThat(result).containsExactly(Giftcard.class);
        }

        @ParameterizedTest
        @ValueSource(strings = { "at.meks.quarkiverse.axon.shared.model", "at.meks.quarkiverse.axon.shared", "at.meks" })
        void includedPackageMatches(String packageName) {
            when(componentDiscovery.enabled()).thenReturn(true);
            when(componentDiscovery.includedPackages()).thenReturn(Optional.of(Set.of(packageName)));
            List<? extends ClassProvider> axonClassBuildItems = List.of(new AggregateBeanBuildItem(Giftcard.class));

            Set<Class<?>> result = ClassDiscovery.classes(axonClassBuildItems, "aggregate", componentDiscovery);

            assertThat(result).containsExactly(Giftcard.class);
        }

        @ParameterizedTest
        @ValueSource(strings = { "at.meks.quarkiverse.axon.shared.commands", "at.meks.quarkiverse.axon.core", "com.meks" })
        void includedPackageDontMatch(String packageName) {
            when(componentDiscovery.enabled()).thenReturn(true);
            when(componentDiscovery.includedPackages()).thenReturn(Optional.of(Set.of(packageName)));
            List<? extends ClassProvider> axonClassBuildItems = List.of(new AggregateBeanBuildItem(Giftcard.class));

            Set<Class<?>> result = ClassDiscovery.classes(axonClassBuildItems, "aggregate", componentDiscovery);

            assertThat(result).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = { "aggregate", "eventhandler", "commandhandler", "queryhandler", "sagahandler" })
        void disabledDiscovery(String type) {
            when(componentDiscovery.enabled()).thenReturn(false);
            List<? extends ClassProvider> axonClassBuildItems = List.of(new AggregateBeanBuildItem(Giftcard.class));

            Set<Class<?>> result = ClassDiscovery.classes(axonClassBuildItems, type, componentDiscovery);

            assertThat(result).isEmpty();
        }

    }

    @Nested
    class AggregateClasses {

        @Mock
        ComponentDiscovery componentDiscovery;

        @BeforeEach
        void setUp() {
            when(discoveryConfiguration.aggregates()).thenReturn(componentDiscovery);
        }

        @Test
        void enabledAggregateDiscovery() {
            when(componentDiscovery.enabled()).thenReturn(true);
            givenClassWithAnnotatedFieldInIndex(AggregateIdentifier.class, Giftcard.class);

            Stream<Class<?>> result = ClassDiscovery.aggregateClasses(beanArchiveIndex, discoveryConfiguration);

            assertThat(result).containsExactly(Giftcard.class);
        }

        @ParameterizedTest
        @ValueSource(strings = { "at.meks.quarkiverse.axon.shared.model", "at.meks.quarkiverse.axon", "at.meks" })
        void includedPackageMatch(String packageName) {
            when(componentDiscovery.enabled()).thenReturn(true);
            when(componentDiscovery.includedPackages()).thenReturn(Optional.of(Set.of(packageName)));
            givenClassWithAnnotatedFieldInIndex(AggregateIdentifier.class, Giftcard.class);

            Stream<Class<?>> result = ClassDiscovery.aggregateClasses(beanArchiveIndex, discoveryConfiguration);

            assertThat(result).containsExactly(Giftcard.class);
        }

        @ParameterizedTest
        @ValueSource(strings = { "at.meks.quarkiverse.axon.shared.query", "at.meks.quarkiverse.axon.core", "com.meks" })
        void includedPackageDontMatch(String packageName) {
            when(componentDiscovery.enabled()).thenReturn(true);
            when(componentDiscovery.includedPackages()).thenReturn(Optional.of(Set.of(packageName)));
            givenClassWithAnnotatedFieldInIndex(AggregateIdentifier.class, Giftcard.class);

            Stream<Class<?>> result = ClassDiscovery.aggregateClasses(beanArchiveIndex, discoveryConfiguration);

            assertThat(result).isEmpty();
        }

        @Test
        void disabledAggregateDiscovery() {
            when(componentDiscovery.enabled()).thenReturn(false);
            Stream<Class<?>> result = ClassDiscovery.aggregateClasses(beanArchiveIndex, discoveryConfiguration);
            assertThat(result).isEmpty();
        }

    }

    private <T, R> void givenClassWithAnnotatedFieldInIndex(Class<T> annotationClass, Class<R> annotatedClass) {
        AnnotationInstance annotationInstance = mock(AnnotationInstance.class,
                Mockito.withSettings().strictness(Strictness.LENIENT));
        AnnotationTarget annotationTarget = mock(AnnotationTarget.class, Mockito.withSettings().strictness(Strictness.LENIENT));
        FieldInfo fieldInfo = mock(FieldInfo.class, Mockito.withSettings().strictness(Strictness.LENIENT));
        ClassInfo classInfo = mock(ClassInfo.class, Mockito.withSettings().strictness(Strictness.LENIENT));

        when(indexView.getAnnotations(annotationClass)).thenReturn(List.of(annotationInstance));
        when(annotationInstance.target()).thenReturn(annotationTarget);
        when(annotationTarget.asField()).thenReturn(fieldInfo);
        when(fieldInfo.declaringClass()).thenReturn(classInfo);
        when(classInfo.asClass()).thenReturn(classInfo);
        when(classInfo.name()).thenReturn(DotName.createSimple(annotatedClass));
    }

    private <T, R> void givenClassWithAnnotatedMethodInIndex(Class<T> annotationClass, Class<R> annotatedClass) {
        AnnotationInstance annotationInstance = mock(AnnotationInstance.class);
        AnnotationTarget annotationTarget = mock(AnnotationTarget.class);
        MethodInfo methodInfo = mock(MethodInfo.class);
        ClassInfo classInfo = mock(ClassInfo.class, Mockito.withSettings().strictness(Strictness.LENIENT));

        when(indexView.getAnnotations(annotationClass)).thenReturn(List.of(annotationInstance));
        when(annotationInstance.target()).thenReturn(annotationTarget);
        when(annotationTarget.asMethod()).thenReturn(methodInfo);
        when(methodInfo.declaringClass()).thenReturn(classInfo);
        when(classInfo.asClass()).thenReturn(classInfo);
        when(classInfo.name()).thenReturn(DotName.createSimple(annotatedClass));
    }

    @Nested
    class CommandHandlersDiscovery {

        @Mock(strictness = Mock.Strictness.LENIENT)
        private ComponentDiscovery componentDiscovery;

        @Mock(strictness = Mock.Strictness.LENIENT)
        private ComponentDiscovery aggregateDiscovery;

        @BeforeEach
        void setUp() {
            when(discoveryConfiguration.commandHandlers()).thenReturn(componentDiscovery);
            when(discoveryConfiguration.aggregates()).thenReturn(aggregateDiscovery);
            when(aggregateDiscovery.enabled()).thenReturn(true);
            givenClassWithAnnotatedFieldInIndex(AggregateIdentifier.class, Giftcard.class);
        }

        @Test
        void enabledDiscoveryUsingADomainService() {
            when(componentDiscovery.enabled()).thenReturn(true);
            givenClassWithAnnotatedMethodInIndex(CommandHandler.class, DomainServiceExample.class);

            Stream<Class<?>> result = ClassDiscovery.commandhandlerClasses(beanArchiveIndex, discoveryConfiguration);

            assertThat(result).containsExactly(DomainServiceExample.class);
        }

        @ParameterizedTest
        @ValueSource(strings = { "at.meks.quarkiverse.axon.shared.model", "at.meks.quarkiverse.axon", "at.meks" })
        void includedPackageMatch(String packageName) {
            when(componentDiscovery.enabled()).thenReturn(true);
            when(componentDiscovery.includedPackages()).thenReturn(Optional.of(Set.of(packageName)));
            givenClassWithAnnotatedMethodInIndex(CommandHandler.class, DomainServiceExample.class);

            Stream<Class<?>> result = ClassDiscovery.commandhandlerClasses(beanArchiveIndex, discoveryConfiguration);

            assertThat(result).containsExactly(DomainServiceExample.class);
        }

        @ParameterizedTest
        @ValueSource(strings = { "at.meks.quarkiverse.axon.shared.query", "at.meks.quarkiverse.axon.core", "com.meks" })
        void includedPackageDontMatch(String packageName) {
            when(componentDiscovery.enabled()).thenReturn(true);
            when(componentDiscovery.includedPackages()).thenReturn(Optional.of(Set.of(packageName)));
            givenClassWithAnnotatedMethodInIndex(CommandHandler.class, DomainServiceExample.class);

            Stream<Class<?>> result = ClassDiscovery.commandhandlerClasses(beanArchiveIndex, discoveryConfiguration);

            assertThat(result).isEmpty();
        }

        @Test
        void disabledAggregateDiscovery() {
            when(componentDiscovery.enabled()).thenReturn(false);
            Stream<Class<?>> result = ClassDiscovery.commandhandlerClasses(beanArchiveIndex, discoveryConfiguration);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class EventHandlersDiscovery {

        @Mock(strictness = Mock.Strictness.LENIENT)
        private ComponentDiscovery componentDiscovery;

        @BeforeEach
        void setUp() {
            when(discoveryConfiguration.eventHandlers()).thenReturn(componentDiscovery);
        }

        @Test
        void enabledDiscoveryUsingADomainService() {
            when(componentDiscovery.enabled()).thenReturn(true);
            givenClassWithAnnotatedMethodInIndex(EventHandler.class, GiftcardQueryHandler.class);

            Stream<Class<?>> result = ClassDiscovery.eventhandlerClasses(beanArchiveIndex, discoveryConfiguration);

            assertThat(result).containsExactly(GiftcardQueryHandler.class);
        }

        @ParameterizedTest
        @ValueSource(strings = { "at.meks.quarkiverse.axon.shared.projection", "at.meks.quarkiverse.axon", "at.meks" })
        void includedPackageMatch(String packageName) {
            when(componentDiscovery.enabled()).thenReturn(true);
            when(componentDiscovery.includedPackages()).thenReturn(Optional.of(Set.of(packageName)));
            givenClassWithAnnotatedMethodInIndex(EventHandler.class, GiftcardQueryHandler.class);

            Stream<Class<?>> result = ClassDiscovery.eventhandlerClasses(beanArchiveIndex, discoveryConfiguration);

            assertThat(result).containsExactly(GiftcardQueryHandler.class);
        }

        @ParameterizedTest
        @ValueSource(strings = { "at.meks.quarkiverse.axon.shared.model", "at.meks.quarkiverse.axon.core", "com.meks" })
        void includedPackageDontMatch(String packageName) {
            when(componentDiscovery.enabled()).thenReturn(true);
            when(componentDiscovery.includedPackages()).thenReturn(Optional.of(Set.of(packageName)));
            givenClassWithAnnotatedMethodInIndex(EventHandler.class, GiftcardQueryHandler.class);

            Stream<Class<?>> result = ClassDiscovery.eventhandlerClasses(beanArchiveIndex, discoveryConfiguration);

            assertThat(result).isEmpty();
        }

        @Test
        void disabledAggregateDiscovery() {
            when(componentDiscovery.enabled()).thenReturn(false);
            Stream<Class<?>> result = ClassDiscovery.eventhandlerClasses(beanArchiveIndex, discoveryConfiguration);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class QueryHandlersDiscovery {

        @Mock(strictness = Mock.Strictness.LENIENT)
        private ComponentDiscovery componentDiscovery;

        @BeforeEach
        void setUp() {
            when(discoveryConfiguration.queryHandlers()).thenReturn(componentDiscovery);
        }

        @Test
        void enabledDiscoveryUsingADomainService() {
            when(componentDiscovery.enabled()).thenReturn(true);
            givenClassWithAnnotatedMethodInIndex(QueryHandler.class, GiftcardQueryHandler.class);

            Stream<Class<?>> result = ClassDiscovery.queryhandlerClasses(beanArchiveIndex, discoveryConfiguration);

            assertThat(result).containsExactly(GiftcardQueryHandler.class);
        }

        @ParameterizedTest
        @ValueSource(strings = { "at.meks.quarkiverse.axon.shared.projection", "at.meks.quarkiverse.axon", "at.meks" })
        void includedPackageMatch(String packageName) {
            when(componentDiscovery.enabled()).thenReturn(true);
            when(componentDiscovery.includedPackages()).thenReturn(Optional.of(Set.of(packageName)));
            givenClassWithAnnotatedMethodInIndex(QueryHandler.class, GiftcardQueryHandler.class);

            Stream<Class<?>> result = ClassDiscovery.queryhandlerClasses(beanArchiveIndex, discoveryConfiguration);

            assertThat(result).containsExactly(GiftcardQueryHandler.class);
        }

        @ParameterizedTest
        @ValueSource(strings = { "at.meks.quarkiverse.axon.shared.model", "at.meks.quarkiverse.axon.core", "com.meks" })
        void includedPackageDontMatch(String packageName) {
            when(componentDiscovery.enabled()).thenReturn(true);
            when(componentDiscovery.includedPackages()).thenReturn(Optional.of(Set.of(packageName)));
            givenClassWithAnnotatedMethodInIndex(QueryHandler.class, GiftcardQueryHandler.class);

            Stream<Class<?>> result = ClassDiscovery.queryhandlerClasses(beanArchiveIndex, discoveryConfiguration);

            assertThat(result).isEmpty();
        }

        @Test
        void disabledAggregateDiscovery() {
            when(componentDiscovery.enabled()).thenReturn(false);
            Stream<Class<?>> result = ClassDiscovery.queryhandlerClasses(beanArchiveIndex, discoveryConfiguration);
            assertThat(result).isEmpty();
        }

    }

    @Nested
    class SagaEventHandlersDiscovery {

        @Mock(strictness = Mock.Strictness.LENIENT)
        private ComponentDiscovery componentDiscovery;

        @BeforeEach
        void setUp() {
            when(discoveryConfiguration.sagaHandlers()).thenReturn(componentDiscovery);
        }

        @Test
        void enabledDiscoveryUsingADomainService() {
            when(componentDiscovery.enabled()).thenReturn(true);
            givenClassWithAnnotatedMethodInIndex(SagaEventHandler.class, CardReturnSaga.class);

            Stream<Class<?>> result = ClassDiscovery.sagaEventhandlerClasses(beanArchiveIndex, discoveryConfiguration);

            assertThat(result).containsExactly(CardReturnSaga.class);
        }

        @ParameterizedTest
        @ValueSource(strings = { "at.meks.quarkiverse.axon.shared.model", "at.meks.quarkiverse.axon", "at.meks" })
        void includedPackageMatch(String packageName) {
            when(componentDiscovery.enabled()).thenReturn(true);
            when(componentDiscovery.includedPackages()).thenReturn(Optional.of(Set.of(packageName)));
            givenClassWithAnnotatedMethodInIndex(SagaEventHandler.class, CardReturnSaga.class);

            Stream<Class<?>> result = ClassDiscovery.sagaEventhandlerClasses(beanArchiveIndex, discoveryConfiguration);

            assertThat(result).containsExactly(CardReturnSaga.class);
        }

        @ParameterizedTest
        @ValueSource(strings = { "at.meks.quarkiverse.axon.shared.projection", "at.meks.quarkiverse.axon.core", "com.meks" })
        void includedPackageDontMatch(String packageName) {
            when(componentDiscovery.enabled()).thenReturn(true);
            when(componentDiscovery.includedPackages()).thenReturn(Optional.of(Set.of(packageName)));
            givenClassWithAnnotatedMethodInIndex(SagaEventHandler.class, CardReturnSaga.class);

            Stream<Class<?>> result = ClassDiscovery.sagaEventhandlerClasses(beanArchiveIndex, discoveryConfiguration);

            assertThat(result).isEmpty();
        }

        @Test
        void disabledAggregateDiscovery() {
            when(componentDiscovery.enabled()).thenReturn(false);
            Stream<Class<?>> result = ClassDiscovery.sagaEventhandlerClasses(beanArchiveIndex, discoveryConfiguration);
            assertThat(result).isEmpty();
        }
    }

}
