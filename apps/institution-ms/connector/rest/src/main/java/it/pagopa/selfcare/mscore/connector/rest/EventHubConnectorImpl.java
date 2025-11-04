package it.pagopa.selfcare.mscore.connector.rest;

import it.pagopa.selfcare.mscore.api.EventHubConnector;
import it.pagopa.selfcare.mscore.connector.rest.client.EventHubRestClient;
import it.pagopa.selfcare.mscore.model.DelegationNotificationToSend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventHubConnectorImpl implements EventHubConnector {

    private final EventHubRestClient eventHubRestClient;

    public EventHubConnectorImpl(EventHubRestClient eventHubRestClient) {
        this.eventHubRestClient = eventHubRestClient;
    }

    @Override
    public boolean sendEvent(DelegationNotificationToSend notification) {
        try {
            eventHubRestClient.sendMessage(notification);
            log.info("Event notification of delegation with id {} sent", notification.getId());
            return true;
        } catch (Exception ex) {
            log.error("Error sending event notification of delegation with id {}", notification.getId(), ex);
            return false;
        }
    }

}
