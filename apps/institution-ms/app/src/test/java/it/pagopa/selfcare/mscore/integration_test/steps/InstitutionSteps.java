package it.pagopa.selfcare.mscore.integration_test.steps;

import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import it.pagopa.selfcare.mscore.connector.dao.InstitutionRepository;
import it.pagopa.selfcare.mscore.connector.dao.PecNotificationRepository;
import it.pagopa.selfcare.mscore.connector.dao.model.InstitutionEntity;
import it.pagopa.selfcare.mscore.connector.dao.model.PecNotificationEntity;
import it.pagopa.selfcare.mscore.connector.dao.model.inner.OnboardingEntity;
import it.pagopa.selfcare.mscore.constant.Origin;
import it.pagopa.selfcare.mscore.constant.RelationshipState;
import it.pagopa.selfcare.mscore.integration_test.utils.SharedStepData;
import org.junit.jupiter.api.Assertions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InstitutionSteps {

    private final SharedStepData sharedStepData;
    private final InstitutionRepository institutionRepository;
    private final PecNotificationRepository pecNotificationRepository;
    private final MongoTemplate mongoTemplate;

    private String mockInstitutionId;

    public InstitutionSteps(SharedStepData sharedStepData,
                            InstitutionRepository institutionRepository,
                            PecNotificationRepository pecNotificationRepository,
                            MongoTemplate mongoTemplate) {
        this.sharedStepData = sharedStepData;
        this.institutionRepository = institutionRepository;
        this.pecNotificationRepository = pecNotificationRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @After("@RemoveInstitutionIdAfterScenario")
    public void removeInstitutionIdAfterScenario(Scenario scenario) {
        Optional.ofNullable(sharedStepData.getResponse().body().jsonPath().getString("id"))
                .ifPresent(institutionRepository::deleteById);
    }

    @After("@RemoveSubunitAndParentInstitutionAfterScenario")
    public void removeSubunitAndParentInstitutionAfterScenario(Scenario scenario) {
        Optional.ofNullable(sharedStepData.getResponse().body().jsonPath().getString("rootParent.id"))
                .ifPresent(institutionRepository::deleteById);
        Optional.ofNullable(sharedStepData.getResponse().body().jsonPath().getString("id"))
                .ifPresent(institutionRepository::deleteById);
    }

    @After("@RemoveMockInstitutionAfterScenario")
    public void removeMockInstitution() {
        Optional.ofNullable(mockInstitutionId).ifPresent(id -> {
            institutionRepository.deleteById(id);
            mongoTemplate.remove(new Query(Criteria.where("institutionId").is(id)), PecNotificationEntity.class);
        });
    }

    @And("A mock institution with id {string}")
    public void createMockInstitutionWithId(String id) {
        final InstitutionEntity entity = new InstitutionEntity();
        entity.setId(id);
        entity.setOrigin(Origin.MOCK);
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
        entity.setOnboarding(List.of(
                createMockOnboardingEntity("prod-io", RelationshipState.ACTIVE, OffsetDateTime.now(), OffsetDateTime.now()),
                createMockOnboardingEntity("prod-pagopa", RelationshipState.ACTIVE, OffsetDateTime.now(), OffsetDateTime.now()),
                createMockOnboardingEntity("prod-idpay", RelationshipState.DELETED, OffsetDateTime.now(), OffsetDateTime.now()),
                createMockOnboardingEntity("prod-pn", RelationshipState.SUSPENDED, OffsetDateTime.now(), OffsetDateTime.now())
        ));
        entity.setDigitalAddress("digital.address@test.com");
        final InstitutionEntity savedEntity = institutionRepository.save(entity);
        mockInstitutionId = savedEntity.getId();
        final PecNotificationEntity pecNotificationEntityIO = new PecNotificationEntity();
        pecNotificationEntityIO.setInstitutionId(mockInstitutionId);
        pecNotificationEntityIO.setProductId("prod-io");
        pecNotificationEntityIO.setModuleDayOfTheEpoch(10);
        pecNotificationEntityIO.setDigitalAddress("digital.address@test.com");
        pecNotificationRepository.save(pecNotificationEntityIO);
        final PecNotificationEntity pecNotificationEntityPA = new PecNotificationEntity();
        pecNotificationEntityPA.setInstitutionId(mockInstitutionId);
        pecNotificationEntityPA.setProductId("prod-pagopa");
        pecNotificationEntityPA.setModuleDayOfTheEpoch(20);
        pecNotificationEntityPA.setDigitalAddress("digital.address@test.com");
        pecNotificationRepository.save(pecNotificationEntityPA);
    }

    private OnboardingEntity createMockOnboardingEntity(String productId, RelationshipState status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        final OnboardingEntity onboardingEntity = new OnboardingEntity();
        onboardingEntity.setProductId(productId);
        onboardingEntity.setStatus(status);
        onboardingEntity.setCreatedAt(createdAt);
        onboardingEntity.setUpdatedAt(updatedAt);
        return onboardingEntity;
    }

    @And("Onboarding of institutionId {string} and productId {string} has createdAt {string}")
    public void checkOnboardingCreatedAt(String institutionId, String productId, String expectedCreatedAt) {
        final InstitutionEntity institution = institutionRepository.findById(institutionId).orElseThrow();
        final OnboardingEntity onboarding = institution.getOnboarding()
                .stream().filter(o -> o.getProductId().equals(productId)).findFirst().orElseThrow();
        Assertions.assertTrue(OffsetDateTime.parse(expectedCreatedAt).isEqual(onboarding.getCreatedAt()));
    }

    @And("Onboardings of institution with id {string} have following states:")
    public void checkOnboardingsStates(String institutionId, Map<String, String> expectedProductState) {
        final InstitutionEntity entity = institutionRepository.findById(institutionId).orElseThrow();
        entity.getOnboarding().forEach(e -> Assertions.assertEquals(expectedProductState.get(e.getProductId()), e.getStatus().name()));
    }

    @And("Count of PecNotification with institutionId {string} is {long}")
    public void checkPecNotificationCount(String institutionId, long expectedCount) {
        long count = mongoTemplate.count(new Query(Criteria.where("institutionId").is(institutionId)), PecNotificationEntity.class);
        Assertions.assertEquals(expectedCount, count);
    }

}
