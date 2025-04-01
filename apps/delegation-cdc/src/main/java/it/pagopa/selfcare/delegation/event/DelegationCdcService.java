package it.pagopa.selfcare.delegation.event;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import com.microsoft.applicationinsights.TelemetryClient;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import io.quarkus.mongodb.ChangeStreamOptions;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.mongodb.reactive.ReactiveMongoCollection;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.configuration.ConfigUtils;
import io.smallrye.mutiny.Multi;
import it.pagopa.selfcare.delegation.event.constant.CdcStartAtConstant;
import it.pagopa.selfcare.delegation.event.entity.DelegationsEntity;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.*;

import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static java.util.Arrays.asList;

@Startup
@Slf4j
@ApplicationScoped
public class DelegationCdcService {
    private static final String COLLECTION_NAME = "Delegations";
    private static final String  DELEGATION_INSERT_FAILURE = "DelegationInsert_failure";
    private static final String ERROR_DURING_SUBSCRIBE_COLLECTION_EXCEPTION_MESSAGE = "Error during subscribe collection, exception: {} , message: {}";
    private static final String EVENT_INSTITUTION_CDC_NAME = "DELEGATION_CDC";
    private static final String OPERATION_NAME = "DELEGATION-CDC-DelegationInsert";

    private final TelemetryClient telemetryClient;
    private final TableClient tableClient;
    private final String mongodbDatabase;
    private final ReactiveMongoClient mongoClient;


    public DelegationCdcService(ReactiveMongoClient mongoClient,
                                     @ConfigProperty(name = "quarkus.mongodb.database") String mongodbDatabase,
                                     TelemetryClient telemetryClient,
                                     TableClient tableClient) {
        this.mongoClient = mongoClient;
        this.mongodbDatabase = mongodbDatabase;
        this.telemetryClient = telemetryClient;
        this.tableClient = tableClient;
        telemetryClient.getContext().getOperation().setName(OPERATION_NAME);
        initOrderStream();
    }

    private void initOrderStream() {
        log.info("Starting initOrderStream ... ");

        //Retrieve last resumeToken for watching collection at specific operation
        String resumeToken = null;

        if (!ConfigUtils.getProfiles().contains("test")) {
            try {
                TableEntity cdcStartAtEntity = tableClient.getEntity(CdcStartAtConstant.CDC_START_AT_PARTITION_KEY, CdcStartAtConstant.CDC_START_AT_ROW_KEY);
                if (Objects.nonNull(cdcStartAtEntity))
                    resumeToken = (String) cdcStartAtEntity.getProperty(CdcStartAtConstant.CDC_START_AT_PROPERTY);
            } catch (TableServiceException e) {
                log.warn("Table StarAt not found, it is starting from now ...");
            }
        }

        // Initialize watching collection
        ReactiveMongoCollection<DelegationsEntity> dataCollection = getCollection();
        ChangeStreamOptions options = new ChangeStreamOptions()
                .fullDocument(FullDocument.UPDATE_LOOKUP);
        if (Objects.nonNull(resumeToken))
            options = options.resumeAfter(BsonDocument.parse(resumeToken));

        Bson match = Aggregates.match(Filters.in("operationType", asList("update", "replace", "insert")));
        Bson project = Aggregates.project(fields(include("_id", "ns", "documentKey", "fullDocument")));
        List<Bson> pipeline = Arrays.asList(match, project);

        Multi<ChangeStreamDocument<DelegationsEntity>> publisher = dataCollection.watch(pipeline, DelegationsEntity.class, options);

        publisher.subscribe().with(
                this::consumerDelegationRepositoryEvent,
                failure -> {
                    log.error(ERROR_DURING_SUBSCRIBE_COLLECTION_EXCEPTION_MESSAGE, failure.toString(), failure.getMessage());
                    constructMapAndTrackEvent(failure.getClass().toString(), "FALSE", DELEGATION_INSERT_FAILURE);
                    Quarkus.asyncExit();
                });
        log.info("Completed initOrderStream ... ");
    }

    private ReactiveMongoCollection<DelegationsEntity> getCollection() {
        return mongoClient
                .getDatabase(mongodbDatabase)
                .getCollection(COLLECTION_NAME, DelegationsEntity.class);
    }

    public void consumerDelegationRepositoryEvent(ChangeStreamDocument<DelegationsEntity> document) {

        DelegationsEntity insertedDelegation = document.getFullDocument();
        assert insertedDelegation != null;
        String delegationId = insertedDelegation.getId();

        log.info("Starting consumerDelegationsRepositoryEvent from Delegation document having id: {}", delegationId);
    }

    private void constructMapAndTrackEvent(String documentKey, String success, String... metrics) {
        Map<String, String> propertiesMap = new HashMap<>();
        propertiesMap.put("documentKey", documentKey);
        propertiesMap.put("success", success);

        Map<String, Double> metricsMap = new HashMap<>();
        Arrays.stream(metrics).forEach(metricName -> metricsMap.put(metricName, 1D));
        telemetryClient.trackEvent(EVENT_INSTITUTION_CDC_NAME, propertiesMap, metricsMap);
    }

}
