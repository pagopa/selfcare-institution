package it.pagopa.selfcare.mscore.connector.rest.config;

import feign.RequestInterceptor;
import it.pagopa.selfcare.commons.connector.rest.config.RestClientBaseConfig;
import it.pagopa.selfcare.mscore.connector.rest.client.EventHubRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Configuration
@Import(RestClientBaseConfig.class)
@EnableFeignClients(clients = EventHubRestClient.class)
@PropertySource("classpath:config/event-hub-rest-client.properties")
public class EventHubRestClientConfig {

    private final String resourceUri;
    private final String keyName;
    private final String key;

    public EventHubRestClientConfig(@Value("${rest-client.event-hub.base-url}") String resourceUri,
                                    @Value("${rest-client.event-hub.keyName}") String keyName,
                                    @Value("${rest-client.event-hub.key}") String key) {
        this.resourceUri = resourceUri;
        this.keyName = keyName;
        this.key = key;
    }

    @Bean
    public RequestInterceptor eventHubRequestInterceptor() {
        return requestTemplate -> {
            try {
                requestTemplate.header("Authorization", getSASToken(resourceUri, keyName, key));
            } catch (Exception e) {
                throw new RuntimeException("Error generating SAS token", e);
            }
        };
    }

    private String getSASToken(String resourceUri, String keyName, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        final long epoch = System.currentTimeMillis() / 1000L;
        final int week = 60 * 60 * 24 * 7;
        final String expiry = Long.toString(epoch + week);

        final String stringToSign = URLEncoder.encode(resourceUri, StandardCharsets.UTF_8) + "\n" + expiry;
        final String signature = getHMAC256(key, stringToSign);
        return "SharedAccessSignature sr=" + URLEncoder.encode(resourceUri, StandardCharsets.UTF_8) + "&sig=" +
                URLEncoder.encode(signature, StandardCharsets.UTF_8) + "&se=" + expiry + "&skn=" + keyName;
    }

    private String getHMAC256(String key, String input) throws InvalidKeyException, NoSuchAlgorithmException {
        final Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        sha256HMAC.init(secretKey);
        Base64.Encoder encoder = Base64.getEncoder();
        return new String(encoder.encode(sha256HMAC.doFinal(input.getBytes(StandardCharsets.UTF_8))));
    }

}
