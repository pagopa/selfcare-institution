package it.pagopa.selfcare.mscore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

@Data
@Configuration
@PropertySource("classpath:config/core-config.properties")
@ConfigurationProperties(prefix = "mscore.institution-send-mail.scheduler")
public class InstitutionSendMailConfig {

    Boolean pecNotificationDisabled;
    Map<String, Integer> products;
}
