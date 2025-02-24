package it.pagopa.selfcare.mscore.integration_test.steps;

import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import it.pagopa.selfcare.mscore.connector.dao.InstitutionRepository;
import it.pagopa.selfcare.mscore.integration_test.utils.SharedStepData;

import java.util.Optional;

public class InstitutionSteps {

    private final SharedStepData sharedStepData;
    private final InstitutionRepository institutionRepository;

    public InstitutionSteps(SharedStepData sharedStepData, InstitutionRepository institutionRepository) {
        this.sharedStepData = sharedStepData;
        this.institutionRepository = institutionRepository;
    }

    @After("@RemoveInstitutionIdAfterScenario")
    public void removeInstitutionIdAfterScenario(Scenario scenario) {
        Optional.ofNullable(sharedStepData.getResponse().body().jsonPath().getString("id"))
                .ifPresent(institutionRepository::deleteById);
    }

}
