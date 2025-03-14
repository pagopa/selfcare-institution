package it.pagopa.selfcare.mscore.connector.dao;

import it.pagopa.selfcare.mscore.api.PecNotificationConnector;
import it.pagopa.selfcare.mscore.connector.dao.model.PecNotificationEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class PecNotificationConnectorImpl implements PecNotificationConnector {

    private final PecNotificationRepository repository;

    public PecNotificationConnectorImpl(PecNotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean findAndDeletePecNotification(String institutionId, String productId) {

        Query query = Query.query(Criteria.where(PecNotificationEntity.Fields.institutionId.name()).is(institutionId)
                .and(PecNotificationEntity.Fields.productId.name()).is(productId));

        List<PecNotificationEntity> pecNotificationEntityList = repository.find(query, PecNotificationEntity.class);

        if(Objects.nonNull(pecNotificationEntityList) && pecNotificationEntityList.size() == 1) {
            repository.delete(pecNotificationEntityList.get(0));
            log.trace("Deleted PecNotification with institutionId: {} and productId: {}", institutionId, productId);
            return true;
        }
        
        if (Objects.nonNull(pecNotificationEntityList) && pecNotificationEntityList.size() > 1) {
        	log.warn("Cannot delete PecNotification with institutionId: {} and productId: {}, because there are multiple entries", institutionId, productId);
        	return false;
        }

        log.warn("Cannot delete PecNotification with institutionId: {} and productId: {}, because it does not exist", institutionId, productId);
        return false;
    }

}
