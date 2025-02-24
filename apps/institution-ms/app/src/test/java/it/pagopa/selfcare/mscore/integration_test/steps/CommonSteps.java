package it.pagopa.selfcare.mscore.integration_test.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import it.pagopa.selfcare.mscore.integration_test.model.JwtData;
import it.pagopa.selfcare.mscore.integration_test.utils.SharedStepData;
import it.pagopa.selfcare.mscore.integration_test.utils.TestDataProvider;
import it.pagopa.selfcare.mscore.integration_test.utils.TestJwtGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class CommonSteps {

    private final SharedStepData sharedStepData;
    private final TestDataProvider testDataProvider;
    private final TestJwtGenerator testJwtGenerator;

    public CommonSteps(SharedStepData sharedStepData, TestDataProvider testDataProvider, TestJwtGenerator testJwtGenerator) {
        this.sharedStepData = sharedStepData;
        this.testDataProvider = testDataProvider;
        this.testJwtGenerator = testJwtGenerator;
    }

    @Before
    public void clearSharedStepData() {
        log.info("Clearing sharedStepData");
        sharedStepData.clear();
    }

    @Given("User login with username {string} and password {string}")
    public void login(String username, String password) {
        JwtData jwtData = testDataProvider.getTestData().getJwtData().stream()
                .filter(data -> data.getUsername().equals(username) && data.getPassword().equals(password))
                .findFirst()
                .orElse(null);
        sharedStepData.setToken(testJwtGenerator.generateToken(jwtData));
    }

    @Given("A bad jwt token")
    public void badToken() {
        sharedStepData.setToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Imp3dF9hMjo3YTo0NjozYjoyYTo2MDo1Njo0MDo4ODphMDo1ZDphNDpmODowMToxZTozZSJ9.eyJmYW1pbHlfbmFtZSI6IlNhcnRvcmkiLCJmaXNjYWxfbnVtYmVyIjoiU1JUTkxNMDlUMDZHNjM1UyIsIm5hbWUiOiJBbnNlbG1vIiwic3BpZF9sZXZlbCI6Imh0dHBzOi8vd3d3LnNwaWQuZ292Lml0L1NwaWRMMiIsImZyb21fYWEiOmZhbHNlLCJ1aWQiOiI1MDk2ZTRjNi0yNWExLTQ1ZDUtOWJkZi0yZmI5NzRhN2MxYzgiLCJsZXZlbCI6IkwyIiwiaWF0IjoxNzM5MzYxMzUzLCJhdWQiOiJhcGkuZGV2LnNlbGZjYXJlLnBhZ29wYS5pdCIsImlzcyI6IlNQSUQiLCJqdGkiOiJfOWE2M2ZiNTQyYzk4NDJjZWMyNmQifQ.X9zoPHuLq6GafRM6zV6hnN09SQ1rL0rFWK5d-RfwJACHam1nPjqX6INx9Qd-_E69GFlr4O1JzzIzc3wfnbIhRlKMVTLjw5xjadc_sxoq-6sH-8Ek_aPeWqL44m_RKcngFCzh-7KrD32wrh4fyC_tdhFbS0SSWjTLgDy0mn3gGPLwFGmv2ASW7xZvw-DfQpsNZhEDJAOQgQ4qC5Lyxo_RriBHDIq1pZvtmW6RkIYsLJ8EGNoOGM4SzUOM3ZSSieh-48DLb8HsDwJgrle6gJJZoqZ0saeAN-7Gy-q55tl3E0hLhfif81RQ_nFH7nc3I9kLffaxWfpH7Oym5F3Nur-btg");
    }

    @And("The following request body:")
    public void setRequestBody(String requestBody) {
        sharedStepData.setRequestBody(requestBody);
    }

    @And("The following path params:")
    public void setPathParams(Map<String, String> pathParams) {
        sharedStepData.setPathParams(pathParams);
    }

    @And("The following query params:")
    public void setQueryParams(Map<String, String> queryParams) {
        sharedStepData.setQueryParams(queryParams);
    }

    @When("I send a POST request to {string}")
    public void sendPostRequest(String url) {
        final String token = sharedStepData.getToken();
        sharedStepData.setResponse(RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .pathParams(Optional.ofNullable(sharedStepData.getPathParams()).orElse(Collections.emptyMap()))
                    .queryParams(Optional.ofNullable(sharedStepData.getQueryParams()).orElse(Collections.emptyMap()))
                    .body(sharedStepData.getRequestBody())
                .when()
                    .post(url)
                .then()
                    .extract()
        );
    }

    @When("I send a HEAD request to {string}")
    public void sendHeadRequest(String url) {
        final String token = sharedStepData.getToken();
        sharedStepData.setResponse(RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + token)
                    .pathParams(Optional.ofNullable(sharedStepData.getPathParams()).orElse(Collections.emptyMap()))
                    .queryParams(Optional.ofNullable(sharedStepData.getQueryParams()).orElse(Collections.emptyMap()))
                .when()
                    .head(url)
                .then()
                    .extract());
    }

    @Then("The status code is {int}")
    public void checkStatusCode(int expectedStatusCode) {
        Assertions.assertEquals(expectedStatusCode, sharedStepData.getResponse().statusCode());
    }

    @And("The response body contains:")
    public void checkResponseBody(Map<String, String> expectedKeyValues) {
        expectedKeyValues.forEach((expectedKey, expectedValue) -> {
            final String currentValue = sharedStepData.getResponse().body().jsonPath().getString(expectedKey);
            Assertions.assertEquals(expectedValue, currentValue, String.format("The field %s does not contain the expected value", expectedKey));
        });
    }

    @And("The response body contains field {string}")
    public void checkResponseBodyKey(String expectedJsonPath) {
        final String currentValue = sharedStepData.getResponse().body().jsonPath().getString(expectedJsonPath);
        Assertions.assertNotNull(currentValue);
    }

    @And("The response body doesn't contain field {string}")
    public void checkResponseBodyMissingKey(String expectedJsonPath) {
        final String currentValue = sharedStepData.getResponse().body().jsonPath().getString(expectedJsonPath);
        Assertions.assertNull(currentValue);
    }

}
