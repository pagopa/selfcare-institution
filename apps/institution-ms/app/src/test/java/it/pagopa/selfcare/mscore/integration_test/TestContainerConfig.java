package it.pagopa.selfcare.mscore.integration_test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

@Component
@Slf4j
public class TestContainerConfig {
    private static volatile ComposeContainer composeContainer;

    public TestContainerConfig() {
        if (composeContainer == null) {
            synchronized (TestContainerConfig.class) {
                if (composeContainer == null) {
                    composeContainer = new ComposeContainer(new File("src/test/resources/docker-compose.yml"))
                            .withLocalCompose(true)
                            .withLogConsumer("azure-cli", new Slf4jLogConsumer(log))
                            .withLogConsumer("azurite", new Slf4jLogConsumer(log))
                            .withLogConsumer("mongodb", new Slf4jLogConsumer(log))
                            .withLogConsumer("mockserver", new Slf4jLogConsumer(log))
                            .waitingFor("azure-cli", Wait.forLogMessage(".*BLOBSTORAGE INITIALIZED.*\\n", 1));
                    composeContainer.start();
                    Runtime.getRuntime().addShutdownHook(new Thread(composeContainer::stop));
                }
            }
        }
    }
}
