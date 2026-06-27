package at.meks.quarkiverse.axon.it;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.axonframework.serialization.Serializer;

import at.meks.quarkiverse.axon.runtime.customizations.AxonSerializerProducer;
import at.meks.quarkiverse.axon.shared.model.Api;

@Path("system")
public class SystemResource {

    @Inject
    DataSource dataSource;

    @Inject
    AxonSerializerProducer axonSerializerProducer;

    @GET
    @Path("snapshots/count")
    @Produces(MediaType.TEXT_PLAIN)
    public long x() {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select count(*) from JdbcSnapshotEventEntry")) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("serialization/axon-roundtrip")
    @Produces(MediaType.TEXT_PLAIN)
    public String axonSerializationRoundTrip() {
        assertRoundTrip(axonSerializerProducer.createSerializer(), new Api.CardIssuedEvent("native-card", 20));
        assertRoundTrip(axonSerializerProducer.createEventSerializer(), new Api.CardRedeemedEvent("native-card", 3));
        assertRoundTrip(axonSerializerProducer.createMessageSerializer(), new Api.IssueCardCommand("native-card", 20));
        return "ok";
    }

    private static <T> void assertRoundTrip(Serializer serializer, T value) {
        var serialized = serializer.serialize(value, String.class);
        Object deserialized = serializer.deserialize(serialized);
        if (!value.equals(deserialized)) {
            throw new IllegalStateException("Axon serializer round trip failed for " + value.getClass().getName());
        }
    }

}
