package at.meks.quarkiverse.axon.runtime.defaults.eventprocessors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.axonframework.messaging.core.annotation.Namespace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.meks.quarkiverse.axon.runtime.defaults.eventprocessors.EventhandlersPerNamespace.Eventhandler;
import at.meks.quarkiverse.axon.runtime.defaults.eventprocessors.EventhandlersPerNamespace.EventhandlersOfANamespace;
import at.meks.quarkiverse.axon.runtime.defaults.eventprocessors.EventhandlersPerNamespace.NamespaceName;

class EventhandlersPerNamespaceTest {

    private Eventhandler1WithNamespaceA eventhandler1WithNamespaceA;
    private Eventhandler2WithNamespaceA eventhandler2WithNamespaceA;
    private Eventhandler1WithNamespaceB eventhandler1WithNamespaceB;
    private Eventhandler1WithNamespaceC eventhandler1WithNamespaceC;
    private Evenhandler1WithoutNamespace evenhandler1WithoutNamespace;
    private Evenhandler2WithoutNamespace evenhandler2WithoutNamespace;
    private EventhandlersPerNamespace eventhandlersPerNamespace;

    @Namespace("A")
    record Eventhandler1WithNamespaceA() {
    }

    @Namespace("A")
    record Eventhandler2WithNamespaceA() {
    }

    @Namespace("B")
    record Eventhandler1WithNamespaceB() {
    }

    @Namespace("C")
    record Eventhandler1WithNamespaceC() {
    }

    record Evenhandler1WithoutNamespace() {
    }

    record Evenhandler2WithoutNamespace() {
    }

    @BeforeEach
    void setUp() {
        eventhandler1WithNamespaceA = new Eventhandler1WithNamespaceA();
        eventhandler2WithNamespaceA = new Eventhandler2WithNamespaceA();
        eventhandler1WithNamespaceB = new Eventhandler1WithNamespaceB();
        eventhandler1WithNamespaceC = new Eventhandler1WithNamespaceC();
        evenhandler1WithoutNamespace = new Evenhandler1WithoutNamespace();
        evenhandler2WithoutNamespace = new Evenhandler2WithoutNamespace();
        eventhandlersPerNamespace = new EventhandlersPerNamespace(
                List.of(eventhandler1WithNamespaceA, eventhandler2WithNamespaceA,
                        eventhandler1WithNamespaceB,
                        eventhandler1WithNamespaceC, evenhandler1WithoutNamespace,
                        evenhandler2WithoutNamespace));
    }

    @Test
    void stream() {
        assertThat(eventhandlersPerNamespace.stream())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new EventhandlersOfANamespace(new NamespaceName("A"),
                                List.of(new Eventhandler(eventhandler1WithNamespaceA),
                                        new Eventhandler(eventhandler2WithNamespaceA))),
                        new EventhandlersOfANamespace(new NamespaceName("B"),
                                List.of(new Eventhandler(eventhandler1WithNamespaceB))),
                        new EventhandlersOfANamespace(new NamespaceName("C"),
                                List.of(new Eventhandler(eventhandler1WithNamespaceC))),
                        new EventhandlersOfANamespace(
                                new NamespaceName("at.meks.quarkiverse.axon.runtime.defaults.eventprocessors"),
                                List.of(new Eventhandler(evenhandler1WithoutNamespace),
                                        new Eventhandler(evenhandler2WithoutNamespace))));
    }

}
