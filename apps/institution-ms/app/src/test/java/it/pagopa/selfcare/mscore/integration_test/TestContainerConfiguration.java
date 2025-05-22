package it.pagopa.selfcare.mscore.integration_test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;

@TestConfiguration
@Slf4j
public class TestContainerConfiguration {

    private ComposeContainer composeContainer;

    @PostConstruct
    public void startContainers() {
        log.info("Starting test containers...");

        composeContainer = new ComposeContainer(new File("src/test/resources/docker-compose.yml"))
                .withLocalCompose(true)
                .withEnv("GITHUB_TOKEN", System.getenv("GITHUB_TOKEN"))
                .withLogConsumer("azure-cli", new Slf4jLogConsumer(log))
                .withLogConsumer("azurite", new Slf4jLogConsumer(log))
                .withLogConsumer("mongodb", new Slf4jLogConsumer(log))
                .withLogConsumer("mockserver", new Slf4jLogConsumer(log))
                .waitingFor("azure-cli", Wait.forLogMessage(".*BLOBSTORAGE INITIALIZED.*\\n", 1));

        composeContainer.start();
        log.info("Test containers started successfully");
    }

    @PreDestroy
    public void stopContainers() {
        if (composeContainer != null) {
            try {
                log.info("Stopping test containers...");
                composeContainer.stop();
                log.info("Test containers stopped");
            } catch (Exception e) {
                log.warn("Error stopping containers: {}", e.getMessage());
            }
        }
    }
}