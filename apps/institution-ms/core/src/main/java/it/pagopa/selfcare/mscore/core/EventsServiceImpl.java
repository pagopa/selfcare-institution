package it.pagopa.selfcare.mscore.core;

import it.pagopa.selfcare.mscore.api.DelegationConnector;
import it.pagopa.selfcare.mscore.api.EventHubConnector;
import it.pagopa.selfcare.mscore.core.mapper.DelegationNotificationMapper;
import it.pagopa.selfcare.mscore.model.delegation.DelegationWithCursorPagination;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class EventsServiceImpl implements EventsService {

    private static final int DELEGATION_PAGE_SIZE = 100;

    private final DelegationConnector delegationConnector;
    private final EventHubConnector eventHubConnector;
    private final DelegationNotificationMapper delegationNotificationMapper;

    @Override
    public void sendDelegationEvents(OffsetDateTime fromDate) {
        DelegationWithCursorPagination delegationsPage = delegationConnector.findFromDate(fromDate, null, DELEGATION_PAGE_SIZE);
        while (delegationsPage.getCursor() != null) {
            delegationsPage.getDelegations().forEach(d -> eventHubConnector.sendEvent(delegationNotificationMapper.toDelegationNotificationToSend(d)));
            delegationsPage = delegationConnector.findFromDate(fromDate, delegationsPage.getCursor(), DELEGATION_PAGE_SIZE);
        }
    }

}
