package it.pagopa.selfcare.mscore.api;

public interface PecNotificationConnector {

    boolean findAndDeletePecNotification(String institutionId, String productId);

}
