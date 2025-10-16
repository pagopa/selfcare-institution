package it.pagopa.selfcare.mscore.web.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import it.pagopa.selfcare.mscore.api.UserRegistryConnector;
import it.pagopa.selfcare.mscore.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Pattern;

@Component
public class EncryptIfTaxCodeDeserializer extends JsonDeserializer<String> {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final UserRegistryConnector userRegistryConnector;

    @Autowired
    public EncryptIfTaxCodeDeserializer(UserRegistryConnector userRegistryConnector) {
        this.userRegistryConnector = userRegistryConnector;
    }

    public EncryptIfTaxCodeDeserializer() {
        this.userRegistryConnector = null;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();

        if (value == null || value.isBlank()) {
            return null;
        }

        if (UUID_PATTERN.matcher(value).matches() && userRegistryConnector != null) {
            User user = userRegistryConnector.getUserByInternalId(value);
            return user != null ? user.getFiscalCode() : null;
        }

        return value;
    }
}
