package it.pagopa.selfcare.mscore.integration_test;

import io.cucumber.spring.CucumberContextConfiguration;
import it.pagopa.selfcare.mscore.SelfCareCoreApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Properties;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@CucumberContextConfiguration
@SpringBootTest(classes = {SelfCareCoreApplication.class, RestAssuredConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "it.pagopa.selfcare.cucumber.utils,it.pagopa.selfcare.mscore.integration_test")
@Slf4j
public class CucumberSuite {

    private static final ComposeContainer composeContainer;

    static {
        log.info("Starting test containers...");

        String dockerComposeFilePath = Paths.get(System.getProperty("user.dir"))
                .toAbsolutePath()
                .toString()
                .replaceFirst("(.*apps).*", "$1/institution-ms/docker-compose.yml");

        composeContainer = new ComposeContainer(new File(dockerComposeFilePath))
                .withLocalCompose(true)
                .waitingFor("azure-cli", Wait.forLogMessage(".*BLOBSTORAGE INITIALIZED.*\\n", 1));
        composeContainer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(composeContainer::stop));

        log.info("Test containers started successfully");
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) throws IOException {
        Properties props = new Properties();
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("application-test.properties")) {
            if (input != null) {
                props.load(input);
            }
        }
        String serverPort = props.getProperty("server.port", "8082");
        registry.add("server.port", () -> serverPort);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("key/public-key.pub");
        if (inputStream == null) {
            throw new IOException("Public key file not found in classpath");
        }
        String publicKey = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        registry.add("JWT_TOKEN_PUBLIC_KEY", () -> publicKey);
    }

}
