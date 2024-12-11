package at.meks.quarkiverse.axon.deployment.interceptor.query;

import static org.assertj.core.api.Assertions.assertThatException;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.axonframework.queryhandling.QueryExecutionException;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import at.meks.quarkiverse.axon.shared.unittest.JavaArchiveTest;
import io.quarkus.arc.Unremovable;
import io.quarkus.test.QuarkusUnitTest;

public class QueryExceptionInterceptorTest {

    @Inject
    QueryGateway queryGateway;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> JavaArchiveTest.javaArchiveBase()
                    .addClasses(FaultyQueryHandler.class, FaultyQuery.class));

    record FaultyQuery(String id) {
    }

    @Unremovable
    @ApplicationScoped
    static class FaultyQueryHandler {

        @QueryHandler
        public Object handle(FaultyQuery query) {
            throw new IllegalArgumentException("oops, I did it again");
        }
    }

    @Test
    void onExceptionWhenCommandIsHandled() {
        assertThatException()
                .isThrownBy(() -> queryGateway.query(new FaultyQuery(UUID.randomUUID().toString()), Object.class).get())
                .isInstanceOf(ExecutionException.class)
                .havingCause()
                .isInstanceOf(QueryExecutionException.class)
                .withMessageContaining(
                        "error while executing query handler for query at.meks.quarkiverse.axon.deployment.interceptor.query.QueryExceptionInterceptorTest$FaultyQuery")
                .withStackTraceContaining("oops, I did it again");
    }
}
