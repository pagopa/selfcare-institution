package it.pagopa.selfcare.mscore.integration_test;

import io.cucumber.spring.CucumberContextConfiguration;
import io.quarkiverse.cucumber.CucumberOptions;
import it.pagopa.selfcare.mscore.SelfCareCoreApplication;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@CucumberContextConfiguration
@CucumberOptions(
        glue = {"it.pagopa.selfcare.mscore.integration_test.steps", "it.pagopa.selfcare.cucumber.utils"}
)
@SpringBootTest(classes = {SelfCareCoreApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
//@ExcludeTags({"FeatureDelegation", "FeatureDelegationV2", "FeatureExternal", "FeatureInstitution", "FeatureManagement", "FeatureOnboarding", "FeatureFake"})
public class CucumberSuite {

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("key/public-key.pub");
        if (inputStream == null) {
            throw new IOException("Public key file not found in classpath");
        }
        String publicKey = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        registry.add("JWT_TOKEN_PUBLIC_KEY", () -> publicKey);
    }

}
