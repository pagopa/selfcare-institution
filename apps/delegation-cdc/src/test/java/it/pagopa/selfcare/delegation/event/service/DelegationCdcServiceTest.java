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
import it.pagopa.selfcare.delegation.event.config.ConfigUtilsBean;
import it.pagopa.selfcare.delegation.event.entity.DelegationsEntity;
import jakarta.inject.Inject;
import org.bson.BsonDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@QuarkusTest
@QuarkusTestResource(MongoTestResource.class)
class DelegationCdcServiceTest {

    @Inject
    DelegationCdcService delegationCdcService;

    @InjectMock
    ConfigUtilsBean configUtilsBean;


    @Test
    void propagateDocumentToConsumers() {
        //given
        ChangeStreamDocument<DelegationsEntity> document = mock(ChangeStreamDocument.class);

        DelegationsEntity delegationsEntity = new DelegationsEntity();
        delegationsEntity.setId("id");

        //when
        when(document.getFullDocument()).thenReturn(delegationsEntity);
        when(document.getDocumentKey()).thenReturn(new BsonDocument());

        final Executable executable = () -> delegationCdcService.consumerDelegationRepositoryEvent(document);

        // then
        assertDoesNotThrow(executable);
    }

    @Test
    void testDelegationCdcServiceConstructorNotTest() {
        // Configura il mock per il TableClient
        TableClient tableClientMock = mock(TableClient.class);
        when(tableClientMock.getEntity(anyString(), anyString())).thenReturn(null);

        // Configura il mock per il ReactiveMongoClient
        ReactiveMongoClient mongoClientMock = mock(ReactiveMongoClient.class);
        ReactiveMongoDatabase mongoDatabase = mock(ReactiveMongoDatabase.class);
        ReactiveMongoCollection<DelegationsEntity> collectionMock = mock(ReactiveMongoCollection.class);
        when(mongoClientMock.getDatabase(anyString())).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection(anyString(), eq(DelegationsEntity.class))).thenReturn(collectionMock);

        //Configura il mock per il TelemetryClient
        TelemetryClient telemetryClient = mock(TelemetryClient.class);
        TelemetryContext context = mock(TelemetryContext.class);
        OperationContext operationContext = mock(OperationContext.class);
        when(telemetryClient.getContext()).thenReturn(context);
        when(context.getOperation()).thenReturn(operationContext);

        Mockito.when(configUtilsBean.getProfiles()).thenReturn(List.of("uat"));

        // Crea l'istanza del servizio
        new DelegationCdcService(mongoClientMock, "testDatabase", telemetryClient, tableClientMock, configUtilsBean);

        // Verifica che il metodo watch sia stato chiamato
        verify(collectionMock).watch(anyList(), eq(DelegationsEntity.class), any(ChangeStreamOptions.class));
    }

    @Test
    void testDelegationCdcServiceConstructorNotTestAndTableEntityNotNull() {
        // Configura il mock per il TableClient
        TableClient tableClientMock = mock(TableClient.class);
        when(tableClientMock.getEntity(anyString(), anyString())).thenReturn(mock(TableEntity.class));

        // Configura il mock per il ReactiveMongoClient
        ReactiveMongoClient mongoClientMock = mock(ReactiveMongoClient.class);
        ReactiveMongoDatabase mongoDatabase = mock(ReactiveMongoDatabase.class);
        ReactiveMongoCollection<DelegationsEntity> collectionMock = mock(ReactiveMongoCollection.class);
        when(mongoClientMock.getDatabase(anyString())).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection(anyString(), eq(DelegationsEntity.class))).thenReturn(collectionMock);

        //Configura il mock per il TelemetryClient
        TelemetryClient telemetryClient = mock(TelemetryClient.class);
        TelemetryContext context = mock(TelemetryContext.class);
        OperationContext operationContext = mock(OperationContext.class);
        when(telemetryClient.getContext()).thenReturn(context);
        when(context.getOperation()).thenReturn(operationContext);

        Mockito.when(configUtilsBean.getProfiles()).thenReturn(List.of("uat"));

        // Crea l'istanza del servizio
        new DelegationCdcService(mongoClientMock, "testDatabase", telemetryClient, tableClientMock, configUtilsBean);

        // Verifica che il metodo watch sia stato chiamato
        verify(collectionMock).watch(anyList(), eq(DelegationsEntity.class), any(ChangeStreamOptions.class));
    }

    @Test
    void testDelegationCdcServiceConstructorNotTestWithException() {
        // Configura il mock per il TableClient
        TableClient tableClientMock = mock(TableClient.class);
        when(tableClientMock.getEntity(anyString(), anyString())).thenThrow(mock(TableServiceException.class));

        // Configura il mock per il ReactiveMongoClient
        ReactiveMongoClient mongoClientMock = mock(ReactiveMongoClient.class);
        ReactiveMongoDatabase mongoDatabase = mock(ReactiveMongoDatabase.class);
        ReactiveMongoCollection<DelegationsEntity> collectionMock = mock(ReactiveMongoCollection.class);
        when(mongoClientMock.getDatabase(anyString())).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection(anyString(), eq(DelegationsEntity.class))).thenReturn(collectionMock);

        //Configura il mock per il TelemetryClient
        TelemetryClient telemetryClient = mock(TelemetryClient.class);
        TelemetryContext context = mock(TelemetryContext.class);
        OperationContext operationContext = mock(OperationContext.class);
        when(telemetryClient.getContext()).thenReturn(context);
        when(context.getOperation()).thenReturn(operationContext);

        Mockito.when(configUtilsBean.getProfiles()).thenReturn(List.of("uat"));

        // Crea l'istanza del servizio
        new DelegationCdcService(mongoClientMock, "testDatabase", telemetryClient, tableClientMock, configUtilsBean);

        // Verifica che il metodo watch sia stato chiamato
        verify(collectionMock).watch(anyList(), eq(DelegationsEntity.class), any(ChangeStreamOptions.class));
    }

    @Test
    void testDelegationCdcServiceConstructorTest() {
        // Configura il mock per il TableClient
        TableClient tableClientMock = mock(TableClient.class);
        when(tableClientMock.getEntity(anyString(), anyString())).thenReturn(null);

        // Configura il mock per il ReactiveMongoClient
        ReactiveMongoClient mongoClientMock = mock(ReactiveMongoClient.class);
        ReactiveMongoDatabase mongoDatabase = mock(ReactiveMongoDatabase.class);
        ReactiveMongoCollection<DelegationsEntity> collectionMock = mock(ReactiveMongoCollection.class);
        when(mongoClientMock.getDatabase(anyString())).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection(anyString(), eq(DelegationsEntity.class))).thenReturn(collectionMock);

        //Configura il mock per il TelemetryClient
        TelemetryClient telemetryClient = mock(TelemetryClient.class);
        TelemetryContext context = mock(TelemetryContext.class);
        OperationContext operationContext = mock(OperationContext.class);
        when(telemetryClient.getContext()).thenReturn(context);
        when(context.getOperation()).thenReturn(operationContext);

        // Crea l'istanza del servizio
        new DelegationCdcService(mongoClientMock, "testDatabase", telemetryClient, tableClientMock, configUtilsBean);

        // Verifica che il metodo watch sia stato chiamato
        verify(collectionMock).watch(anyList(), eq(DelegationsEntity.class), any(ChangeStreamOptions.class));
    }

}
