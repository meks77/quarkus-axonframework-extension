package at.meks.quarkiverse.axon.server.runtime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.meks.quarkiverse.axon.server.runtime.AxonServers.AxonServer;

@ExtendWith(MockitoExtension.class)
class AxonServersTest {

    @InjectMocks
    AxonServers axonServers;

    @Mock
    QuarkusAxonServerConfiguration axonServerConfiguration;

    @Test
    void onlyOneServerNameWithPortIsProvided() {
        given(axonServerConfiguration.servers()).willReturn("axon-server-node1:8943");

        assertThat(axonServers.axonServers())
                .containsExactly(new AxonServer("axon-server-node1", 8943));
        assertThat(axonServers.axonServersAsConnectionString())
                .isEqualTo("axon-server-node1:8943");
    }

    @Test
    void onlyOneServerNameWithoutPortIsProvided() {
        given(axonServerConfiguration.defaultGrpcPort()).willReturn(8646);
        given(axonServerConfiguration.servers()).willReturn("axon-server-node1");

        assertThat(axonServers.axonServers())
                .containsExactly(new AxonServer("axon-server-node1", 8646));
        assertThat(axonServers.axonServersAsConnectionString())
                .isEqualTo("axon-server-node1:8646");
    }

    @Test
    void onlyOneServerNameWithDefaultPortIsProvided() {
        given(axonServerConfiguration.defaultGrpcPort()).willReturn(8024);
        given(axonServerConfiguration.servers())
                .willReturn("axon-server-node1:8943,axon-server-node2,axon-server-node3:8474,axon-server-node4:9876");

        assertThat(axonServers.axonServers())
                .containsExactlyInAnyOrder(
                        new AxonServer("axon-server-node1", 8943),
                        new AxonServer("axon-server-node2", 8024),
                        new AxonServer("axon-server-node3", 8474),
                        new AxonServer("axon-server-node4", 9876));
        assertThat(axonServers.axonServersAsConnectionString())
                .isEqualTo("axon-server-node1:8943,axon-server-node2:8024,axon-server-node3:8474,axon-server-node4:9876");
    }

    @Test
    void portIsNotAnInteger() {
        given(axonServerConfiguration.servers()).willReturn("axon-server-node1:" + Long.MAX_VALUE);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> axonServers.axonServers())
                .withMessageStartingWith("Invalid port");
    }

}
