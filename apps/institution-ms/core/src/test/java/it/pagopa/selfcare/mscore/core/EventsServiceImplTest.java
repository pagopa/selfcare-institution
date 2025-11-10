package it.pagopa.selfcare.mscore.core;

import it.pagopa.selfcare.mscore.api.DelegationConnector;
import it.pagopa.selfcare.mscore.api.EventHubConnector;
import it.pagopa.selfcare.mscore.constant.DelegationState;
import it.pagopa.selfcare.mscore.constant.DelegationType;
import it.pagopa.selfcare.mscore.constant.EventType;
import it.pagopa.selfcare.mscore.core.mapper.DelegationNotificationMapper;
import it.pagopa.selfcare.mscore.core.mapper.DelegationNotificationMapperImpl;
import it.pagopa.selfcare.mscore.model.DelegationNotificationToSend;
import it.pagopa.selfcare.mscore.model.delegation.Delegation;
import it.pagopa.selfcare.mscore.model.delegation.DelegationWithCursorPagination;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventsServiceImplTest {

    @InjectMocks
    private EventsServiceImpl eventsServiceImpl;

    @Mock
    private DelegationConnector delegationConnector;

    @Mock
    private EventHubConnector eventHubConnector;

    @Spy
    private DelegationNotificationMapper delegationNotificationMapper = new DelegationNotificationMapperImpl();

    @Test
    void sendDelegationEventsTest() {
        final DelegationWithCursorPagination delegationsPage0 = new DelegationWithCursorPagination();
        delegationsPage0.setDelegations(List.of(
                createDelegation(OffsetDateTime.now(), null, null),
                createDelegation(OffsetDateTime.now(), OffsetDateTime.now(), null)
        ));
        delegationsPage0.setCursor(0L);
        when(delegationConnector.findFromDate(any(), isNull(), any())).thenReturn(delegationsPage0);

        final DelegationWithCursorPagination delegationsPage1 = new DelegationWithCursorPagination();
        delegationsPage1.setDelegations(List.of(
                createDelegation(OffsetDateTime.now(), OffsetDateTime.now(), OffsetDateTime.now())
        ));
        delegationsPage1.setCursor(1L);
        when(delegationConnector.findFromDate(any(), eq(0L), any())).thenReturn(delegationsPage1);

        final DelegationWithCursorPagination delegationsPage2 = new DelegationWithCursorPagination();
        delegationsPage2.setDelegations(new ArrayList<>());
        delegationsPage2.setCursor(null);
        when(delegationConnector.findFromDate(any(), eq(1L), any())).thenReturn(delegationsPage2);

        eventsServiceImpl.sendDelegationEvents(OffsetDateTime.now());

        final ArgumentCaptor<DelegationNotificationToSend> captor = ArgumentCaptor.forClass(DelegationNotificationToSend.class);
        verify(eventHubConnector, times(3)).sendEvent(captor.capture());
        final List<DelegationNotificationToSend> sentNotifications = captor.getAllValues();
        assertEquals(3, sentNotifications.size());
        assertEquals(1, sentNotifications.stream().filter(n -> EventType.ADD.equals(n.getEventType())).count());
        assertEquals(2, sentNotifications.stream().filter(n -> EventType.UPDATE.equals(n.getEventType())).count());

        verify(delegationConnector, times(1)).findFromDate(any(), isNull(), any());
        verify(delegationConnector, times(1)).findFromDate(any(), eq(0L), any());
        verify(delegationConnector, times(1)).findFromDate(any(), eq(1L), any());
        verify(delegationConnector, times(3)).findFromDate(any(), any(), any());
    }

    private Delegation createDelegation(OffsetDateTime createdAt, OffsetDateTime updatedAt, OffsetDateTime closedAt) {
        final Delegation delegation = new Delegation();
        delegation.setId("mockId");
        delegation.setFrom("mockFrom");
        delegation.setInstitutionFromName("mockInstitutionFromName");
        delegation.setInstitutionToName("mockInstitutionToName");
        delegation.setInstitutionFromRootName("mockInstitutionFromRootName");
        delegation.setType(DelegationType.PT);
        delegation.setTo("mockTo");
        delegation.setProductId("mockProductId");
        delegation.setInstitutionType(InstitutionType.PA);
        delegation.setTaxCode("mockTaxCode");
        delegation.setToTaxCode("mockToTaxCode");
        delegation.setFromTaxCode("mockFromTaxCode");
        delegation.setBrokerType(InstitutionType.PT);
        delegation.setBrokerTaxCode("mockBrokerTaxCode");
        delegation.setFromSubunitCode("mockFromSubunitCode");
        delegation.setToSubunitCode("mockToSubunitCode");
        delegation.setStatus(DelegationState.ACTIVE);
        delegation.setCreatedAt(createdAt);
        delegation.setUpdatedAt(updatedAt);
        delegation.setIsTest(Boolean.TRUE);
        delegation.setClosedAt(closedAt);
        return delegation;
    }

}
