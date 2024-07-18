package it.pagopa.selfcare.institution.config;

import io.smallrye.config.ConfigMapping;
import lombok.Data;

import java.util.Map;

@Data
@ConfigMapping(prefix = "institution-send-mail.scheduled-product")
public class ProductConfig {

    private Map<String, Integer> productMap;
}
