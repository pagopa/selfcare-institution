package it.pagopa.selfcare.delegation.event.service;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.mongodb.MongoTestResource;
import it.pagopa.selfcare.delegation.event.DelegationCdcService;
import it.pagopa.selfcare.delegation.event.entity.DelegationsEntity;
import jakarta.inject.Inject;
import org.bson.BsonDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@QuarkusTestResource(MongoTestResource.class)
class DelegationCdcServiceTest {

    @Inject
    DelegationCdcService delegationCdcService;


    @Test
    void propagateDocumentToConsumers_withChangeUserMailFalse() {
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

}
