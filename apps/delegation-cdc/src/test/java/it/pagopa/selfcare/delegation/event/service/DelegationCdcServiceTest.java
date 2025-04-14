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
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.delegation.event.DelegationCdcService;
import it.pagopa.selfcare.delegation.event.config.ConfigUtilsBean;
import it.pagopa.selfcare.delegation.event.entity.*;
import it.pagopa.selfcare.delegation.event.entity.mapper.DelegationMapper;
import it.pagopa.selfcare.delegation.event.repository.DelegationRepository;
import it.pagopa.selfcare.delegation.event.repository.InstitutionRepository;
import jakarta.inject.Inject;
import org.bson.BsonDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@QuarkusTest
@QuarkusTestResource(MongoTestResource.class)
class DelegationCdcServiceTest {

    public static final int RETRY_MIN_BACK_OFF = 10;
    public static final int RETRY_MAX_BACK_OFF = 12;
    public static final int MAX_RETRY = 3;

    @Inject
    DelegationCdcService delegationCdcService;

    @InjectMock
    ConfigUtilsBean configUtilsBean;

    @InjectMock
    InstitutionRepository institutionRepository;

    @InjectMock
    DelegationRepository delegationRepository;

    @Inject
    DelegationMapper delegationMapper;


    @Test
    void propagateDocumentToConsumers() {
        //given
        ChangeStreamDocument<DelegationsEntity> document = mock(ChangeStreamDocument.class);
        DelegationsEntity delegationsEntity = new DelegationsEntity();
        delegationsEntity.setId("id");
        delegationsEntity.setType(DelegationType.PT);
        delegationsEntity.setProductId("prod-pagopa");
        delegationsEntity.setFrom("institutionId");
        delegationsEntity.setTo("toInstitutionId");

        Institution institution = new Institution();
        OnboardingEntity onboardingEntity = new OnboardingEntity();
        onboardingEntity.setStatus(RelationshipState.ACTIVE);
        onboardingEntity.setCreatedAt("2024-07-09T12:40:21.414946089Z");
        onboardingEntity.setProductId("prod-pagopa");
        onboardingEntity.setIsAggregator(Boolean.TRUE);
        institution.setOnboarding(List.of(onboardingEntity));

        DelegationsEntity delegationEA = new DelegationsEntity();
        delegationEA.setId("idEA");
        delegationEA.setFrom("institutionIdFromEA");
        delegationEA.setTo("institutionIdToEA");
        delegationEA.setProductId("prod-pagopa");

        BsonDocument bsonDocument = mock(BsonDocument.class);

        //when
        when(document.getFullDocument()).thenReturn(delegationsEntity);
        when(document.getDocumentKey()).thenReturn(bsonDocument);
        when(bsonDocument.toJson()).thenReturn("toJson");
        when(institutionRepository.findInstitutionById(anyString())).thenReturn(Uni.createFrom().item(institution));
        when(delegationRepository.getInstitutionsAlreadyPresent(anyString(), anyString())).thenReturn(Multi.createFrom().empty());
        when(delegationRepository.getDelegationsEA(anyString(), anyString())).thenReturn(Multi.createFrom().items(delegationEA));
        when(delegationRepository.insertDelegations(ArgumentMatchers.any())).thenReturn(Uni.createFrom().nullItem());

        // then
        final Executable executable = () -> delegationCdcService.consumerDelegationRepositoryEvent(document);
        assertDoesNotThrow(executable);

        // verify
        verify(institutionRepository).findInstitutionById(anyString());
        verify(delegationRepository).getInstitutionsAlreadyPresent(anyString(), anyString());
        verify(delegationRepository).getDelegationsEA(anyString(), anyString());
        verify(delegationRepository).insertDelegations(ArgumentMatchers.any());
    }


    @Test
    void propagateDocumentToConsumers_institutionIsNull() {
        //given
        ChangeStreamDocument<DelegationsEntity> document = mock(ChangeStreamDocument.class);
        DelegationsEntity delegationsEntity = new DelegationsEntity();
        delegationsEntity.setId("id");
        delegationsEntity.setType(DelegationType.PT);
        delegationsEntity.setProductId("prod-pagopa");
        delegationsEntity.setFrom("institutionId");
        delegationsEntity.setTo("toInstitutionId");

        //when
        when(document.getFullDocument()).thenReturn(delegationsEntity);
        when(document.getDocumentKey()).thenReturn(new BsonDocument());
        when(institutionRepository.findInstitutionById(anyString())).thenReturn(Uni.createFrom().nullItem());

        // then
        final Executable executable = () -> delegationCdcService.consumerDelegationRepositoryEvent(document);
        assertDoesNotThrow(executable);

        // Verify
        verify(institutionRepository).findInstitutionById(anyString());
        verify(delegationRepository, never()).getInstitutionsAlreadyPresent(anyString(), anyString());
        verify(delegationRepository, never()).getDelegationsEA(anyString(), anyString());
        verify(delegationRepository, never()).insertDelegations(ArgumentMatchers.any());
    }

    @Test
    void propagateDocumentToConsumers_isAggregatorNotTrue() {
        //given
        ChangeStreamDocument<DelegationsEntity> document = mock(ChangeStreamDocument.class);
        DelegationsEntity delegationsEntity = new DelegationsEntity();
        delegationsEntity.setId("id");
        delegationsEntity.setType(DelegationType.PT);
        delegationsEntity.setProductId("prod-pagopa");
        delegationsEntity.setFrom("institutionId");
        delegationsEntity.setTo("toInstitutionId");

        Institution institution = new Institution();
        OnboardingEntity onboardingEntity = new OnboardingEntity();
        onboardingEntity.setStatus(RelationshipState.ACTIVE);
        onboardingEntity.setProductId("prod-pagopa");
        onboardingEntity.setIsAggregator(Boolean.TRUE);
        onboardingEntity.setCreatedAt("2024-07-09T12:40:21.414946089Z");
        OnboardingEntity onboardingEntity2 = new OnboardingEntity();
        onboardingEntity2.setStatus(RelationshipState.ACTIVE);
        onboardingEntity2.setProductId("prod-pagopa");
        onboardingEntity2.setIsAggregator(Boolean.FALSE);
        onboardingEntity2.setCreatedAt("2024-10-12T18:31:21.314946089Z");
        institution.setOnboarding(List.of(onboardingEntity, onboardingEntity2));

        //when
        when(document.getFullDocument()).thenReturn(delegationsEntity);
        when(document.getDocumentKey()).thenReturn(new BsonDocument());
        when(institutionRepository.findInstitutionById(anyString())).thenReturn(Uni.createFrom().item(institution));

        // then
        final Executable executable = () -> delegationCdcService.consumerDelegationRepositoryEvent(document);
        assertDoesNotThrow(executable);

        // Verify
        verify(institutionRepository).findInstitutionById(anyString());
        verify(delegationRepository, never()).getInstitutionsAlreadyPresent(anyString(), anyString());
        verify(delegationRepository, never()).getDelegationsEA(anyString(), anyString());
        verify(delegationRepository, never()).insertDelegations(ArgumentMatchers.any());
    }


    @Test
    void testConsumerDelegationRepositoryEvent_Failure() {
        // given
        ChangeStreamDocument<DelegationsEntity> document = mock(ChangeStreamDocument.class);
        DelegationsEntity delegationsEntity = new DelegationsEntity();
        delegationsEntity.setId("delegationId");
        delegationsEntity.setType(DelegationType.PT);
        delegationsEntity.setProductId("prod-pagopa");
        delegationsEntity.setFrom("institutionId");
        delegationsEntity.setTo("toInstitutionId");

        Institution institution = new Institution();
        OnboardingEntity onboardingEntity = new OnboardingEntity();
        onboardingEntity.setStatus(RelationshipState.ACTIVE);
        onboardingEntity.setProductId("prod-pagopa");
        onboardingEntity.setIsAggregator(Boolean.TRUE);
        institution.setOnboarding(List.of(onboardingEntity));

        DelegationsEntity delegationEA = new DelegationsEntity();
        delegationEA.setId("idEA");
        delegationEA.setFrom("institutionIdFromEA");
        delegationEA.setTo("institutionIdToEA");
        delegationEA.setProductId("prod-pagopa");

        BsonDocument bsonDocument = mock(BsonDocument.class);

        // when
        when(document.getFullDocument()).thenReturn(delegationsEntity);
        when(document.getDocumentKey()).thenReturn(bsonDocument);
        when(bsonDocument.toJson()).thenReturn("toJson");
        when(institutionRepository.findInstitutionById(anyString())).thenThrow(new RuntimeException());

        // then
        final Executable executable = () -> delegationCdcService.consumerDelegationRepositoryEvent(document);
        assertThrows(RuntimeException.class, executable);

        // verify
        verify(institutionRepository).findInstitutionById(anyString());
        verify(delegationRepository, never()).getInstitutionsAlreadyPresent(anyString(), anyString());
        verify(delegationRepository, never()).getDelegationsEA(anyString(), anyString());
        verify(delegationRepository, never()).insertDelegations(ArgumentMatchers.any());
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
        new DelegationCdcService(mongoClientMock, "testDatabase", telemetryClient, tableClientMock, configUtilsBean, institutionRepository, delegationRepository, RETRY_MIN_BACK_OFF, RETRY_MAX_BACK_OFF, MAX_RETRY);

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
        new DelegationCdcService(mongoClientMock, "testDatabase", telemetryClient, tableClientMock, configUtilsBean, institutionRepository, delegationRepository, RETRY_MIN_BACK_OFF, RETRY_MAX_BACK_OFF, MAX_RETRY);

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
        new DelegationCdcService(mongoClientMock, "testDatabase", telemetryClient, tableClientMock, configUtilsBean, institutionRepository, delegationRepository, RETRY_MIN_BACK_OFF, RETRY_MAX_BACK_OFF, MAX_RETRY);

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
        new DelegationCdcService(mongoClientMock, "testDatabase", telemetryClient, tableClientMock, configUtilsBean, institutionRepository, delegationRepository, RETRY_MIN_BACK_OFF, RETRY_MAX_BACK_OFF, MAX_RETRY);

        // Verifica che il metodo watch sia stato chiamato
        verify(collectionMock).watch(anyList(), eq(DelegationsEntity.class), any(ChangeStreamOptions.class));
    }

}
