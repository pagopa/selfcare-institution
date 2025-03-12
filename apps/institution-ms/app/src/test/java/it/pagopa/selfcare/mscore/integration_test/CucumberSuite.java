package it.pagopa.selfcare.mscore.integration_test;

import io.cucumber.spring.CucumberContextConfiguration;
import it.pagopa.selfcare.mscore.SelfCareCoreApplication;
import org.junit.platform.suite.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@CucumberContextConfiguration
@SpringBootTest(classes = {SelfCareCoreApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@ExcludeTags({"FeatureDelegation", "FeatureDelegationV2", "FeatureExternal", "FeatureInstitution", "FeatureManagement", "FeatureOnboarding", "FeatureFake"})
public class CucumberSuite {

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) throws IOException {
        final Path filePath = Paths.get("src/test/resources/key/public-key.pub");
        final String publicKey = String.join("", Files.readAllLines(filePath));
        registry.add("JWT_TOKEN_PUBLIC_KEY", () -> publicKey);
    }

}
