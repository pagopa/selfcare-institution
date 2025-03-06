package it.pagopa.selfcare.mscore.integration_test.steps;

import io.cucumber.java.After;
import io.cucumber.java.en.And;
import it.pagopa.selfcare.mscore.connector.dao.DelegationRepository;
import it.pagopa.selfcare.mscore.connector.dao.InstitutionRepository;
import it.pagopa.selfcare.mscore.connector.dao.model.DelegationEntity;
import it.pagopa.selfcare.mscore.connector.dao.model.InstitutionEntity;
import it.pagopa.selfcare.mscore.constant.DelegationState;
import it.pagopa.selfcare.mscore.constant.DelegationType;
import it.pagopa.selfcare.mscore.constant.Origin;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import org.junit.jupiter.api.Assertions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.OffsetDateTime;
import java.util.Optional;

public class DelegationSteps {

    private final InstitutionRepository institutionRepository;
    private final DelegationRepository delegationRepository;
    private final MongoTemplate mongoTemplate;

    public DelegationSteps(InstitutionRepository institutionRepository,
                           DelegationRepository delegationRepository,
                           MongoTemplate mongoTemplate) {
        this.institutionRepository = institutionRepository;
        this.delegationRepository = delegationRepository;
        this.mongoTemplate = mongoTemplate;
    }

    private String mockInstitutionId1;
    private String mockInstitutionId2;
    private String mockDelegationId;

    @After("@RemovePairOfMockInstitutionAfterScenario")
    public void removePairOfMockInstitution() {
        Optional.ofNullable(mockInstitutionId1).ifPresent(institutionRepository::deleteById);
        Optional.ofNullable(mockInstitutionId2).ifPresent(institutionRepository::deleteById);
    }

    @After("@RemoveCreatedDelegationAfterScenario")
    public void removeCreatedDelegation() {
        Optional.ofNullable(mockInstitutionId1).ifPresent(id1 -> Optional.ofNullable(mockInstitutionId2).ifPresent(id2 -> {
            mongoTemplate.remove(new Query(Criteria.where("from").is(id1).and("to").is(id2)), DelegationEntity.class);
        }));
        Optional.ofNullable(mockDelegationId).ifPresent(delegationRepository::deleteById);
    }

    @And("A pair of mock institutions with id {string} and {string}")
    public void createPairOfMockInstitutionWithId(String id1, String id2) {
        final InstitutionEntity entity1 = new InstitutionEntity();
        entity1.setId(id1);
        entity1.setOrigin(Origin.MOCK);
        entity1.setCreatedAt(OffsetDateTime.now());
        entity1.setUpdatedAt(OffsetDateTime.now());
        entity1.setTaxCode("112233");
        entity1.setInstitutionType(InstitutionType.PA);
        final InstitutionEntity savedEntity1 = institutionRepository.save(entity1);
        mockInstitutionId1 = savedEntity1.getId();

        final InstitutionEntity entity2 = new InstitutionEntity();
        entity2.setId(id2);
        entity2.setOrigin(Origin.MOCK);
        entity2.setCreatedAt(OffsetDateTime.now());
        entity2.setUpdatedAt(OffsetDateTime.now());
        entity2.setTaxCode("445566");
        entity2.setInstitutionType(InstitutionType.GSP);
        final InstitutionEntity savedEntity2 = institutionRepository.save(entity2);
        mockInstitutionId2 = savedEntity2.getId();
    }

    @And("A mock delegation of type {} with productId {string} for institution with id {string} and {string} with status {}")
    public void createMockDelegation(DelegationType delegationType, String productId, String id1, String id2, DelegationState status) {
        final InstitutionEntity fromInstitution = institutionRepository.findById(id1).orElseThrow();
        final InstitutionEntity toInstitution = institutionRepository.findById(id2).orElseThrow();
        final DelegationEntity delegation = new DelegationEntity();
        delegation.setId("123456");
        delegation.setFrom(fromInstitution.getId());
        delegation.setTo(toInstitution.getId());
        delegation.setInstitutionFromName("From Institution");
        delegation.setInstitutionFromRootName("From Root Institution");
        delegation.setInstitutionToName("To Institution");
        delegation.setFromTaxCode(fromInstitution.getTaxCode());
        delegation.setToTaxCode(toInstitution.getTaxCode());
        delegation.setFromType(fromInstitution.getInstitutionType().name());
        delegation.setToType(toInstitution.getInstitutionType().name());
        delegation.setProductId(productId);
        delegation.setStatus(status);
        delegation.setType(delegationType);
        final DelegationEntity savedDelegation = delegationRepository.save(delegation);
        mockDelegationId = savedDelegation.getId();
    }

    @And("The delegation flag for institution {string} is {} on db")
    public void checkDelegationFlag(String institutionId, Boolean expectedValue) {
        final InstitutionEntity institution = institutionRepository.findById(institutionId).orElseThrow();
        Assertions.assertEquals(expectedValue, institution.isDelegation());
    }

    @And("The delegation from institution {string} to institution {string} was saved to db successfully")
    public void checkCreatedDelegation(String institutionFromId, String institutionToId) {
        final InstitutionEntity fromInstitution = institutionRepository.findById(institutionFromId).orElseThrow();
        final InstitutionEntity toInstitution = institutionRepository.findById(institutionToId).orElseThrow();
        final DelegationEntity delegation = mongoTemplate.findOne(new Query(Criteria.where("from").is(institutionFromId).and("to").is(institutionToId)), DelegationEntity.class);
        Assertions.assertNotNull(delegation);
        Assertions.assertEquals(fromInstitution.getTaxCode(), delegation.getFromTaxCode());
        Assertions.assertEquals(toInstitution.getTaxCode(), delegation.getToTaxCode());
        Assertions.assertEquals(fromInstitution.getInstitutionType().name(), delegation.getFromType());
        Assertions.assertEquals(toInstitution.getInstitutionType().name(), delegation.getToType());
        Assertions.assertEquals(DelegationState.ACTIVE, delegation.getStatus());
    }

}
