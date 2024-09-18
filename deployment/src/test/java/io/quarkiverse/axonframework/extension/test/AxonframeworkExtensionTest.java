package io.quarkiverse.axonframework.extension.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import jakarta.inject.Inject;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class AxonframeworkExtensionTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Inject
    EventGateway eventGateway;
    @Inject
    EventBus eventBus;
    @Inject
    CommandGateway commandGateway;
    @Inject
    CommandBus commandBus;

    @Test
    public void eventGatewayIsProduced() {
        assertNotNull(eventGateway);
    }

    @Test
    public void eventBusIsProduced() {
        assertNotNull(eventBus);
    }

    @Test
    public void commandGatewayIsProduced() {
        assertNotNull(commandGateway);
    }

    @Test
    public void commandBusIsProduced() {
        assertNotNull(commandBus);
    }

    @Test
    public void eventIsPersisted() {
        eventGateway.publish(new ExampleEvent(UUID.randomUUID().toString(), "whatever"));
    }

    public record ExampleEvent(String id, String value) {

    }
}
