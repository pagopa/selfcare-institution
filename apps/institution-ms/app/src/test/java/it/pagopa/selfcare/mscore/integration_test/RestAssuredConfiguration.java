package it.pagopa.selfcare.mscore.integration_test;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.PostConstruct;

@TestConfiguration
@Slf4j
public class RestAssuredConfiguration {

    public RestAssuredConfiguration(
            @Value("${rest-assured.base-url}") String baseUrl,
            @Value("${rest-assured.port}") int port) {

        log.info("Configuring RestAssured with baseURI: {} and port: {}", baseUrl, port);
        RestAssured.baseURI = baseUrl;
        RestAssured.port = port;
        log.info("RestAssured configured successfully");
    }
}