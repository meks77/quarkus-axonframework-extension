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

import org.axonframework.conversion.Converter;

import com.fasterxml.jackson.databind.JsonNode;

import at.meks.quarkiverse.axon.runtime.customizations.AxonConverterProducer;
import at.meks.quarkiverse.axon.shared.model.Api;

@Path("system")
public class SystemResource {

    @Inject
    DataSource dataSource;

    @Inject
    AxonConverterProducer axonConverterProducer;

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
        assertRoundTrip(axonConverterProducer.createGeneralConverter(), new Api.CardIssuedEvent("native-card", 20));
        assertRoundTrip(axonConverterProducer.createEventConverter(), new Api.CardRedeemedEvent("native-card", 3));
        assertRoundTrip(axonConverterProducer.createMessageConverter(), new Api.IssueCardCommand("native-card", 20));
        return "ok";
    }

    private static <T> void assertRoundTrip(Converter converter, T value) {
        JsonNode serialized = converter.convert(value, JsonNode.class);
        Object deserialized = converter.convert(serialized, value.getClass());
        if (!value.equals(deserialized)) {
            throw new IllegalStateException("Axon converter round trip failed for " + value.getClass().getName());
        }
    }

}
