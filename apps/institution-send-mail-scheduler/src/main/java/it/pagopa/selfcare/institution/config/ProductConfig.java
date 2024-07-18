package it.pagopa.selfcare.institution.config;

import io.smallrye.config.ConfigMapping;
import lombok.Data;

import java.util.Map;

@ConfigMapping(prefix = "institution-send-mail.scheduler")
public interface ProductConfig {
    Map<String, Integer> products();
}
