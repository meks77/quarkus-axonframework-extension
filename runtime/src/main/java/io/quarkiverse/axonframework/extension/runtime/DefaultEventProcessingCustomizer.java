package io.quarkiverse.axonframework.extension.runtime;

import static at.meks.validation.args.ArgValidator.validate;
import static io.quarkiverse.axonframework.extension.runtime.AxonConfiguration.TokenStoreType.JDBC;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import javax.sql.DataSource;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.axonframework.axonserver.connector.event.axon.PersistentStreamMessageSource;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventhandling.tokenstore.jdbc.*;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.messaging.SubscribableMessageSource;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.axoniq.axonserver.connector.event.PersistentStreamProperties;
import io.quarkus.arc.DefaultBean;

@Dependent
@DefaultBean
class DefaultEventProcessingCustomizer implements EventProcessingCustomizer {

    @Inject
    AxonConfiguration axonConfiguration;

    @Inject
    ScheduledExecutorService executorService;

    @Inject
    Instance<DataSource> dataSource;

    @ConfigProperty(name = "quarkus.datasource.db-kind", defaultValue = "none")
    String dbKind;

    @Override
    public void configureEventProcessing(EventProcessingConfigurer eventProcessingConfigurer) {
        if (axonConfiguration.eventhandling().defaultMode() == Mode.SUBSCRIBING) {
            eventProcessingConfigurer.usingSubscribingEventProcessors();
        } else if (axonConfiguration.eventhandling().defaultMode() == Mode.PERSISTENT_STREAM) {
            eventProcessingConfigurer.usingSubscribingEventProcessors();
            eventProcessingConfigurer
                    .configureDefaultSubscribableMessageSource(this::defaultPersistentStreamMessageSource);
            // for later: custom processor per handler group
            //            eventProcessingConfigurer.registerSubscribingEventProcessor(${handler group name},
            //                    conf -> new PersistentStreamMessageSource("eventstore", conf,
            //                            streamProperties, executorService, persistentStreamConf.batchSize()));
        } else if (axonConfiguration.eventhandling().defaultMode() == Mode.TRACKING) {
            eventProcessingConfigurer.usingTrackingEventProcessors();
            configureTrackingEventProcessor(eventProcessingConfigurer);
        } else if (axonConfiguration.eventhandling().defaultMode() == Mode.POOLED) {
            eventProcessingConfigurer.usingPooledStreamingEventProcessors();
            configurePooledEventProcessor(eventProcessingConfigurer);
        }

    }

    private SubscribableMessageSource<EventMessage<?>> defaultPersistentStreamMessageSource(Configuration conf) {
        var streamConf = axonConfiguration.eventhandling().defaultPersistentStream();
        PersistentStreamProperties streamProperties = persistentStreamProperties(streamConf);
        return new PersistentStreamMessageSource(streamConf.messageSourceName(), conf, streamProperties,
                executorService, streamConf.batchSize(), streamConf.context());
    }

    private PersistentStreamProperties persistentStreamProperties(AxonConfiguration.PersistentStreamConf persistentStreamConf) {
        return new PersistentStreamProperties(
                persistentStreamConf.streamname(),
                persistentStreamConf.segments(),
                SequencingPolicy.PER_AGGREGATE.axonName(),
                Collections.emptyList(),
                persistentStreamConf.initialPosition(),
                nullIfNone(persistentStreamConf.filter()));
    }

    private String nullIfNone(String filter) {
        return "none".equals(filter) ? null : filter;
    }

    private void configureTrackingEventProcessor(EventProcessingConfigurer eventProcessingConfigurer) {
        AxonConfiguration.StreamingProcessorConf streamingProcessorConf = axonConfiguration.eventhandling()
                .defaultStreamingProcessor();
        configureAndSetupTokenstore(eventProcessingConfigurer, streamingProcessorConf);

        AxonConfiguration.TrackingProcessorConf trackingProcessorConf = streamingProcessorConf.trackingProcessor();
        int threadCount = trackingProcessorConf.threadCount();
        validate().that(threadCount).isGreater(0);
        var trackingEventProcessorConfiguration = TrackingEventProcessorConfiguration
                .forParallelProcessing(threadCount);

        trackingEventProcessorConfiguration
                .andInitialTrackingToken(messageSource -> createToken(messageSource, streamingProcessorConf.initialPosition()));

        Optional.of(streamingProcessorConf.batchSize())
                .filter(size -> size > 1)
                .ifPresent(trackingEventProcessorConfiguration::andBatchSize);

        Optional.of(streamingProcessorConf.initialSegments())
                .filter(segments -> segments >= 1)
                .ifPresent(trackingEventProcessorConfiguration::andInitialSegmentsCount);

        Optional.of(trackingProcessorConf.tokenClaim().interval())
                .filter(interval -> interval > 0)
                .ifPresent(interval -> trackingEventProcessorConfiguration.andTokenClaimInterval(interval,
                        trackingProcessorConf.tokenClaim().timeUnit()));

        eventProcessingConfigurer.registerTrackingEventProcessorConfiguration(
                conf -> trackingEventProcessorConfiguration);
    }

    private void configureAndSetupTokenstore(EventProcessingConfigurer eventProcessingConfigurer,
            AxonConfiguration.StreamingProcessorConf streamingProcessorConf) {
        if (dataSource.isResolvable() && streamingProcessorConf.tokenstore().type() == JDBC) {
            eventProcessingConfigurer.registerTokenStore(conf -> {
                TokenSchema tokenSchema = TokenSchema.builder().build();
                JdbcTokenStore store = JdbcTokenStore.builder()
                        .connectionProvider(() -> dataSource.get().getConnection())
                        .serializer(conf.serializer())
                        .schema(tokenSchema)
                        .build();
                autoCreateJdbcTokenTable(tokenSchema, store);
                return store;
            });

        }
    }

    private TrackingToken createToken(StreamableMessageSource<TrackedEventMessage<?>> messageSource,
            InitialPosition startPosition) {
        if (startPosition == InitialPosition.HEAD) {
            return messageSource.createHeadToken();
        } else if (startPosition == InitialPosition.TAIL) {
            return messageSource.createTailToken();
        }
        throw new IllegalArgumentException(
                "The intial position configuration of the tracking event processor must be head or tail.");
    }

    private void configurePooledEventProcessor(EventProcessingConfigurer eventProcessingConfigurer) {
        AxonConfiguration.StreamingProcessorConf streamingProcessorConf = axonConfiguration.eventhandling()
                .defaultStreamingProcessor();
        configureAndSetupTokenstore(eventProcessingConfigurer, streamingProcessorConf);

        EventProcessingConfigurer.PooledStreamingProcessorConfiguration psepConfig = (config, builder) -> {
            builder
                    .name(streamingProcessorConf.pooledProcessor().name())
                    .initialToken(messageSource -> createToken(messageSource,
                            streamingProcessorConf.initialPosition()));
            Optional.of(streamingProcessorConf.batchSize())
                    .filter(size -> size > 0)
                    .ifPresent(builder::batchSize);
            Optional.of(streamingProcessorConf.initialSegments())
                    .filter(segments -> segments > 0)
                    .ifPresent(builder::initialSegmentCount);
            Optional.of(streamingProcessorConf.pooledProcessor().maxClaimedSegments())
                    .filter(segments -> segments > 0)
                    .ifPresent(builder::maxClaimedSegments);
            if (streamingProcessorConf.pooledProcessor()
                    .enabledCoordinatorClaimExtension()) {
                builder.enableCoordinatorClaimExtension();
            }
            return builder;
        };

        eventProcessingConfigurer.registerPooledStreamingEventProcessorConfiguration(psepConfig);
    }

    private void autoCreateJdbcTokenTable(TokenSchema tokenSchema, JdbcTokenStore store) {
        if (!axonConfiguration.eventhandling().defaultStreamingProcessor().tokenstore().autocreateTableForJdbcToken()) {
            return;
        }
        TokenTableFactory tokenTableFactory;
        boolean dbIsOracle = false;
        boolean tableExists = false;
        if (dbKind.equals("postgresql")) {
            tokenTableFactory = PostgresTokenTableFactory.INSTANCE;
        } else if (dbKind.equals("oracle")) {
            dbIsOracle = true;
            tokenTableFactory = Oracle11TokenTableFactory.INSTANCE;
            try (Connection connection = dataSource.get().getConnection();
                    ResultSet tables = connection.getMetaData().getTables(null, null, tokenSchema.tokenTable(), null)) {
                tableExists = tables.next();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            tokenTableFactory = GenericTokenTableFactory.INSTANCE;
        }
        if (!dbIsOracle || !tableExists) {
            store.createSchema(tokenTableFactory);
        }
    }
}
