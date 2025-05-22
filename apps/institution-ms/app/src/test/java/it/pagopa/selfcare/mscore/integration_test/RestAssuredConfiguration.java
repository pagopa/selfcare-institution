package it.pagopa.selfcare.mscore.integration_test;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.PostConstruct;

@TestConfiguration
@DependsOn("testContainerConfiguration") // Ensure containers start first
@Slf4j
public class RestAssuredConfiguration {

    @Value("${rest-assured.base-url:http://localhost}")
    private String restAssuredBaseUrl;

    @Value("${rest-assured.port:8082}")
    private int restAssuredPort;

    @PostConstruct
    public void configureRestAssured() {
        log.info("Configuring RestAssured with baseURI: {} and port: {}", restAssuredBaseUrl, restAssuredPort);

        RestAssured.baseURI = restAssuredBaseUrl;
        RestAssured.port = restAssuredPort;

        log.info("RestAssured configured successfully");
    }
}