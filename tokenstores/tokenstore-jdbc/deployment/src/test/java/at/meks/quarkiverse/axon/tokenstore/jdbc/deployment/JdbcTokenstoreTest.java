package at.meks.quarkiverse.axon.tokenstore.jdbc.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import jakarta.inject.Inject;

import org.awaitility.Awaitility;
import org.jspecify.annotations.NonNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.Pool;

public abstract class JdbcTokenstoreTest extends JavaArchiveTest {

    @Inject
    Pool client;

    @Inject
    ObjectMapper objectMapper;

    @Override
    protected final void assertOthers() {
        int segmentCount = client.query("SELECT count(*) FROM " + getTokenTabelName())
                .execute()
                .map(rowSet -> rowSet.iterator().next().getInteger(0))
                .await().indefinitely();
        assertThat(segmentCount).isGreaterThanOrEqualTo(1);

        waitUntilProcessorsHaveProcessedEvents();

        List<Integer> globalIndexes = selectGlobalIndexOfTokens();
        Integer maxGlobalIndex = globalIndexes.stream().max(Integer::compareTo).orElseThrow();

        assertThat(maxGlobalIndex).isGreaterThanOrEqualTo(4);
    }

    private void waitUntilProcessorsHaveProcessedEvents() {
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(
                        queryTokenText()
                                .map(text -> text.contains("reset"))
                                .collect().asList()
                                .await().indefinitely())
                        .containsOnly(false));
    }

    private Multi<String> queryTokenText() {
        return client.query("select token from " + getTokenTabelName())
                .execute()
                .onItem()
                .transformToMulti(Multi.createFrom()::iterable)
                .map(row -> row.getBuffer("token").getBytes())
                .map(String::new);
    }

    private List<Integer> selectGlobalIndexOfTokens() {
        return queryTokenText()
                .map(this::toJsonNode)
                .map(jsonNode -> jsonNode.get("globalIndex").asInt())
                .collect().asList()
                .await().indefinitely();
    }

    protected abstract @NonNull String getTokenTabelName();

    private JsonNode toJsonNode(String text) {
        try {
            return objectMapper.readTree(text);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
