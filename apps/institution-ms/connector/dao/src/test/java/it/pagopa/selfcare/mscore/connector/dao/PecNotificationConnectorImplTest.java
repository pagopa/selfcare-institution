package it.pagopa.selfcare.mscore.connector.dao;

import it.pagopa.selfcare.mscore.connector.dao.model.PecNotificationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {PecNotificationConnectorImpl.class})
@ExtendWith(MockitoExtension.class)
class PecNotificationConnectorImplTest {

    @Mock
    private PecNotificationRepository repository;

    @InjectMocks
    private PecNotificationConnectorImpl pecNotificationConnector;

    private String institutionId;
    private String productId;
    private PecNotificationEntity pecNotificationEntity;

    @BeforeEach
    void setUp() {
        institutionId = UUID.randomUUID().toString();
        productId = "prod-io";
        pecNotificationEntity = new PecNotificationEntity();
        pecNotificationEntity.setInstitutionId(institutionId);
        pecNotificationEntity.setProductId(productId);
        pecNotificationEntity.setDigitalAddress("digitalAddress@test.com");
        pecNotificationEntity.setModuleDayOfTheEpoch(1);
    }

    @Test
    void findAndDeletePecNotification_success() {
        when(repository.find(any(), eq(PecNotificationEntity.class)))
                .thenReturn(Collections.singletonList(pecNotificationEntity));

        boolean result = pecNotificationConnector.findAndDeletePecNotification(institutionId, productId);

        assertTrue(result);
        verify(repository, times(1)).delete(pecNotificationEntity);
    }

    @Test
    void findAndDeletePecNotification_multipleEntries() {
        when(repository.find(any(), eq(PecNotificationEntity.class)))
                .thenReturn(List.of(pecNotificationEntity, pecNotificationEntity));

        boolean result = pecNotificationConnector.findAndDeletePecNotification(institutionId, productId);

        assertFalse(result);
        verify(repository, never()).delete(any());
    }

    @Test
    void findAndDeletePecNotification_notExist() {
    	List<PecNotificationEntity> pecNotificationList = Collections.emptyList();
        when(repository.find(any(), eq(PecNotificationEntity.class)))
                .thenReturn(pecNotificationList);

        boolean result = pecNotificationConnector.findAndDeletePecNotification(institutionId, productId);

        assertFalse(result);
        verify(repository, never()).delete(any());
    }

}
