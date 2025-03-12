package it.pagopa.selfcare.mscore.integration_test.steps;

import io.cucumber.java.en.And;
import it.pagopa.selfcare.mscore.integration_test.utils.SharedStepData;
import it.pagopa.selfcare.mscore.web.model.institution.BulkInstitutions;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class ManagementSteps {

    private final SharedStepData sharedStepData;

    private BulkInstitutions bulkInstitutions;

    public ManagementSteps(SharedStepData sharedStepData) {
        this.sharedStepData = sharedStepData;
    }

    @And("I convert the response to BulkInstitutions")
    public void setBulkInstitutions() {
        bulkInstitutions = sharedStepData.getResponse().body().as(BulkInstitutions.class);
    }

    @And("The institutions found are:")
    public void checkBulkInstitutionsFound(List<String> foundList) {
        final Set<String> found = new HashSet<>(foundList);
        Assertions.assertTrue(bulkInstitutions.getFound().stream().allMatch(bi -> found.contains(bi.getId())));
        Assertions.assertEquals(found.size(), bulkInstitutions.getFound().size());
    }

    @And("The institutions not found are:")
    public void checkBulkInstitutionsNotFound(List<String> notFoundList) {
        final Set<String> notFound = new HashSet<>(notFoundList);
        Assertions.assertTrue(notFound.containsAll(bulkInstitutions.getNotFound()));
        Assertions.assertEquals(notFound.size(), bulkInstitutions.getNotFound().size());
    }

    @And("No institutions in found")
    public void checkBulkInstitutionsFoundEmpty() {
        Assertions.assertEquals(0, bulkInstitutions.getFound().size());
    }

    @And("No institutions in notFound")
    public void checkBulkInstitutionsNotFoundEmpty() {
        Assertions.assertEquals(0, bulkInstitutions.getNotFound().size());
    }

}
