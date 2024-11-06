package it.pagopa.selfcare.mscore.api;

import it.pagopa.selfcare.mscore.model.pecnotification.PecNotification;

public interface PecNotificationConnector {

    boolean findAndDeletePecNotification(String institutionId, String productId);

    void insertPecNotification(PecNotification pecNotification);
}
