package it.pagopa.selfcare.mscore.integration_test.utils;

import io.restassured.response.ExtractableResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Getter
@Setter
public class SharedStepData {

    private String token;
    private String requestBody;
    private Map<String, String> pathParams;
    private Map<String, String> queryParams;
    private ExtractableResponse<?> response;

    public void clear() {
        this.token = null;
        this.requestBody = null;
        this.pathParams = null;
        this.queryParams = null;
        this.response = null;
    }

}
