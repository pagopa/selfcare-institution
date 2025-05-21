package it.pagopa.selfcare.mscore.integration_test;

import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@DependsOn("testContainerConfig")
public class CucumberConfig {

    public CucumberConfig(@Value("${rest-assured.base-url}") String restAssuredBaseUrl, @Value("${rest-assured.port}")int restAssuredPort) {
        RestAssured.baseURI = restAssuredBaseUrl;
        RestAssured.port = restAssuredPort;
    }

}
