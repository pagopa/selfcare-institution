package it.pagopa.selfcare.mscore.integration_test.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.selfcare.mscore.integration_test.model.TestData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
@Slf4j
public class TestDataProvider {

    private final TestData testData;

    public TestDataProvider() throws IOException {
        testData = readTestData();
    }

    public TestData getTestData() {
        return testData;
    }

    private TestData readTestData() throws IOException {
        log.info("Reading test data");
        return new ObjectMapper().readValue(new File("src/test/resources/testData.json"), TestData.class);
    }

}
