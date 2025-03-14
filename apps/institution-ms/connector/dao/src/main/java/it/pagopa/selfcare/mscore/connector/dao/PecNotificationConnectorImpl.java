package it.pagopa.selfcare.mscore.connector.dao;

import it.pagopa.selfcare.mscore.api.PecNotificationConnector;
import it.pagopa.selfcare.mscore.connector.dao.model.PecNotificationEntity;
import it.pagopa.selfcare.mscore.connector.dao.model.mapper.PecNotificationEntityMapper;
import it.pagopa.selfcare.mscore.model.pecnotification.PecNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PecNotificationConnectorImpl implements PecNotificationConnector {

    private static final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = validatorFactory.getValidator();

    private final PecNotificationRepository repository;
    private final PecNotificationEntityMapper pecNotificationMapper;


    public PecNotificationConnectorImpl(PecNotificationRepository repository, PecNotificationEntityMapper pecNotificationMapper) {
        this.repository = repository;
        this.pecNotificationMapper = pecNotificationMapper;
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
        	log.trace("Cannot delete PecNotification with institutionId: {} and productId: {}, because there are multiple entries", institutionId, productId);
        	return false;
        }

        log.trace("Cannot delete PecNotification with institutionId: {} and productId: {}, because it does not exist", institutionId, productId);
        return false;
    }

    @Override
    public void insertPecNotification(PecNotification pecNotification){

        PecNotificationEntity pecNotificationEntity = pecNotificationMapper.convertToPecNotificationEntity(pecNotification);

        if(repository.existsByInstitutionIdAndProductId(pecNotificationEntity.getInstitutionId(), pecNotificationEntity.getProductId())){
        	log.trace("Cannot insert the PecNotification: {}, as it already exists in the collection", pecNotification.toString());
            return;
        }


        Set<ConstraintViolation<PecNotificationEntity>> validations = validator.validate(pecNotificationEntity);
        if (!validations.isEmpty()){
            log.warn("Cannot insert the PecNotification: {}, ", validations.stream()
                    .map(v -> String.format("%s %s", v.getPropertyPath().toString(), v.getMessage()))
                    .collect(Collectors.joining(",")));
            return;
        }

        repository.insert(pecNotificationEntity);
        log.trace("Inserted PecNotification: {}", pecNotification.toString());
    }

}
