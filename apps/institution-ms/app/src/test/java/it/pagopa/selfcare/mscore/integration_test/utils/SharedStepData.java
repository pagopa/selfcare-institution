package it.pagopa.selfcare.mscore.integration_test.utils;

import io.restassured.response.ExtractableResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class SharedStepData {

    private String token;
    private String requestBody;
    private ExtractableResponse<?> response;

}
