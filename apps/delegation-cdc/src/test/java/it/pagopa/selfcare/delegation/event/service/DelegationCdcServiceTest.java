package it.pagopa.selfcare.delegation.event.service;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.extensibility.context.OperationContext;
import com.microsoft.applicationinsights.telemetry.TelemetryContext;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import io.quarkus.mongodb.ChangeStreamOptions;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.mongodb.reactive.ReactiveMongoCollection;
import io.quarkus.mongodb.reactive.ReactiveMongoDatabase;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.mongodb.MongoTestResource;
import it.pagopa.selfcare.delegation.event.DelegationCdcService;
import it.pagopa.selfcare.delegation.event.entity.DelegationsEntity;
import jakarta.inject.Inject;
import org.bson.BsonDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@QuarkusTestResource(MongoTestResource.class)
class DelegationCdcServiceTest {

    @Inject
    DelegationCdcService delegationCdcService;

    @InjectMock
    TableClient tableClient;

    @InjectMock
    TelemetryClient telemetryClient;


    @BeforeEach
    void setUp() {
        reset(tableClient, telemetryClient);
    }

    @Test
    void shouldNotThrowErrorWhenConsumingEvent() {
        // given
        ChangeStreamDocument<DelegationsEntity> document = mock(ChangeStreamDocument.class);
        DelegationsEntity entity = new DelegationsEntity();
        entity.setId(UUID.randomUUID().toString());

        when(document.getFullDocument()).thenReturn(entity);
        when(document.getDocumentKey()).thenReturn(new BsonDocument());

        // when + then
        assertDoesNotThrow(() -> delegationCdcService.consumerDelegationRepositoryEvent(document));
    }

    @Test
    void shouldHandleResumeTokenWhenEntityExists() {
        // given
        ReactiveMongoClient mockMongoClient = mock(ReactiveMongoClient.class);
        ReactiveMongoDatabase mockDatabase = mock(ReactiveMongoDatabase.class);
        ReactiveMongoCollection<DelegationsEntity> mockCollection = mock(ReactiveMongoCollection.class);

        TelemetryClient mockTelemetryClient = mock(TelemetryClient.class);
        TelemetryContext mockTelemetryContext = mock(TelemetryContext.class);
        OperationContext mockOperationContext = mock(OperationContext.class);

        when(mockMongoClient.getDatabase(anyString())).thenReturn(mockDatabase);
        when(mockDatabase.getCollection(anyString(), eq(DelegationsEntity.class))).thenReturn(mockCollection);

        when(mockTelemetryClient.getContext()).thenReturn(mockTelemetryContext);
        when(mockTelemetryContext.getOperation()).thenReturn(mockOperationContext);

        TableEntity mockEntity = new TableEntity("cdcStartAtPartitionKey", "cdcStartAtRowKey")
                .addProperty("cdcStartAt", "{ \"_data\": \"resumeTokenMock\" }");
        when(tableClient.getEntity(anyString(), anyString())).thenReturn(mockEntity);

        // when + then
        assertDoesNotThrow(() -> {
            DelegationCdcService service = new DelegationCdcService(
                    mockMongoClient,
                    "test",
                    mockTelemetryClient,
                    tableClient
            );
        });
    }

    @Test
    void shouldHandleMissingEntityWithWarning() {
        // given
        ReactiveMongoClient mockMongoClient = mock(ReactiveMongoClient.class);
        ReactiveMongoDatabase mockDatabase = mock(ReactiveMongoDatabase.class);
        ReactiveMongoCollection<DelegationsEntity> mockCollection = mock(ReactiveMongoCollection.class);

        TelemetryClient mockTelemetryClient = mock(TelemetryClient.class);
        TelemetryContext mockTelemetryContext = mock(TelemetryContext.class);
        OperationContext mockOperationContext = mock(OperationContext.class);

        when(mockMongoClient.getDatabase(anyString())).thenReturn(mockDatabase);
        when(mockDatabase.getCollection(anyString(), eq(DelegationsEntity.class))).thenReturn(mockCollection);

        when(mockTelemetryClient.getContext()).thenReturn(mockTelemetryContext);
        when(mockTelemetryContext.getOperation()).thenReturn(mockOperationContext);

        when(tableClient.getEntity(anyString(), anyString())).thenThrow(TableServiceException.class);

        // when + then
        assertDoesNotThrow(() -> {
            DelegationCdcService service = new DelegationCdcService(
                    mockMongoClient,
                    "test",
                    mockTelemetryClient,
                    tableClient
            );
        });
    }

    @Test
    void shouldHandleValidResumeToken() {
        // given
        ReactiveMongoClient mockMongoClient = mock(ReactiveMongoClient.class);
        ReactiveMongoDatabase mockDatabase = mock(ReactiveMongoDatabase.class);
        ReactiveMongoCollection<DelegationsEntity> mockCollection = mock(ReactiveMongoCollection.class);

        TelemetryClient mockTelemetryClient = mock(TelemetryClient.class);
        TelemetryContext mockTelemetryContext = mock(TelemetryContext.class);
        OperationContext mockOperationContext = mock(OperationContext.class);

        when(mockMongoClient.getDatabase(anyString())).thenReturn(mockDatabase);
        when(mockDatabase.getCollection(anyString(), eq(DelegationsEntity.class))).thenReturn(mockCollection);

        when(mockTelemetryClient.getContext()).thenReturn(mockTelemetryContext);
        when(mockTelemetryContext.getOperation()).thenReturn(mockOperationContext);

        // Mock valore valido per resumeToken
        String validResumeToken = "{ \"_data\": \"validResumeTokenMock\" }";
        TableEntity mockEntity = new TableEntity("cdcStartAtPartitionKey", "cdcStartAtRowKey")
                .addProperty("cdcStartAt", validResumeToken);
        when(tableClient.getEntity(anyString(), anyString())).thenReturn(mockEntity);

        // when + then
        assertDoesNotThrow(() -> {
            DelegationCdcService service = new DelegationCdcService(
                    mockMongoClient,
                    "test-db",
                    mockTelemetryClient,
                    tableClient
            );
        });
    }

    @Test
    void shouldHandleMissingResumeToken() {
        // given
        ReactiveMongoClient mockMongoClient = mock(ReactiveMongoClient.class);
        ReactiveMongoDatabase mockDatabase = mock(ReactiveMongoDatabase.class);
        ReactiveMongoCollection<DelegationsEntity> mockCollection = mock(ReactiveMongoCollection.class);

        TelemetryClient mockTelemetryClient = mock(TelemetryClient.class);
        TelemetryContext mockTelemetryContext = mock(TelemetryContext.class);
        OperationContext mockOperationContext = mock(OperationContext.class);

        when(mockMongoClient.getDatabase(anyString())).thenReturn(mockDatabase);
        when(mockDatabase.getCollection(anyString(), eq(DelegationsEntity.class))).thenReturn(mockCollection);

        when(mockTelemetryClient.getContext()).thenReturn(mockTelemetryContext);
        when(mockTelemetryContext.getOperation()).thenReturn(mockOperationContext);

        // Mock valore null per resumeToken
        when(tableClient.getEntity(anyString(), anyString())).thenThrow(TableServiceException.class);

        // when + then
        assertDoesNotThrow(() -> {
            DelegationCdcService service = new DelegationCdcService(
                    mockMongoClient,
                    "test-db",
                    mockTelemetryClient,
                    tableClient
            );
        });
    }

    @Test
    void shouldHandleErrorDuringStreamSubscription() {
        // given
        ReactiveMongoClient mockMongoClient = mock(ReactiveMongoClient.class);
        ReactiveMongoDatabase mockDatabase = mock(ReactiveMongoDatabase.class);
        ReactiveMongoCollection<DelegationsEntity> mockCollection = mock(ReactiveMongoCollection.class);

        TelemetryClient mockTelemetryClient = mock(TelemetryClient.class);
        TelemetryContext mockTelemetryContext = mock(TelemetryContext.class);
        OperationContext mockOperationContext = mock(OperationContext.class);

        when(mockMongoClient.getDatabase(anyString())).thenReturn(mockDatabase);
        when(mockDatabase.getCollection(anyString(), eq(DelegationsEntity.class))).thenReturn(mockCollection);

        when(mockTelemetryClient.getContext()).thenReturn(mockTelemetryContext);
        when(mockTelemetryContext.getOperation()).thenReturn(mockOperationContext);

        // Mock del publisher che simula errore
        when(mockCollection.watch(anyList(), eq(DelegationsEntity.class), any(ChangeStreamOptions.class)))
                .thenThrow(new RuntimeException("Simulated error"));

        // when + then
        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            DelegationCdcService service = new DelegationCdcService(
                    mockMongoClient,
                    "test-db",
                    mockTelemetryClient,
                    tableClient
            );
        });

        assertEquals("Simulated error", thrownException.getMessage());
    }

}