package it.pagopa.selfcare.institution.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import it.pagopa.selfcare.institution.entity.PecNotification;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PecNotificationsRepository implements PanacheMongoRepositoryBase<PecNotification, String> {
}
