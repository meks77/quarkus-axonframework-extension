package at.meks.quarkiverse.axon.server.deployment.persistentstreams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.eventhandling.SubscribingEventProcessor;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.server.runtime.QuarkusAxonServerConfiguration;
import io.axoniq.axonserver.connector.AxonServerConnection;
import io.axoniq.axonserver.grpc.streams.StreamStatus;
import io.quarkus.test.QuarkusUnitTest;

public class DifferentEventprocessorConfigTest extends PersistentStreamProcessorTest {

    @RegisterExtension
    static final QuarkusUnitTest config = application(
            javaArchiveBase().addAsResource(propertiesFile("/persistenstreams/differentProcessorConfigs.properties"),
                    "application.properties"));

    @Inject
    QuarkusAxonServerConfiguration axonServerConfiguration;

    @Override
    protected void assertPersistedStreamConfigurations(Map<String, SubscribingEventProcessor> list) {
        AxonServerConnection axonServerConnection = configuration.getComponent(AxonServerConnectionManager.class)
                .getConnection(axonServerConfiguration.context());
        List<StreamStatus> persistentStreams = axonServerConnection.eventChannel().persistentStreams().join();
        List<StreamStatus> relevantPersistentStream = persistentStreams.stream()
                .filter(streamStatus -> streamStatus.getStreamId().startsWith("PersistentStreamsTestPropChanges"))
                .toList();
        assertThat(relevantPersistentStream).hasSize(3);
        relevantPersistentStream
                .forEach(this::assertSegmentCount);
    }

    private void assertSegmentCount(StreamStatus persistentStream) {
        String streamId = persistentStream.getStreamId();
        String shortenedStreamId = streamId.substring("PersistentStreamsTestPropChanges-".length());
        int segmentsCount = persistentStream.getSegmentsCount();
        switch (shortenedStreamId) {
            case "GiftCardInMemory" -> assertThat(segmentsCount).isEqualTo(1);
            case "at.meks.quarkiverse.axon.shared.projection" -> assertThat(segmentsCount).isEqualTo(2);
            case "at.meks.quarkiverse.axon.shared.projection2" -> assertThat(segmentsCount).isEqualTo(3);
            default -> fail("Unexpected stream id " + shortenedStreamId);
        }
    }
}
