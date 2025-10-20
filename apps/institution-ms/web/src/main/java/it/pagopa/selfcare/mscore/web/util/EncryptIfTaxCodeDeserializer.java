package it.pagopa.selfcare.mscore.web.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import it.pagopa.selfcare.mscore.api.UserRegistryConnector;
import it.pagopa.selfcare.mscore.model.user.User;

import java.io.IOException;
import java.util.regex.Pattern;

public class EncryptIfTaxCodeDeserializer extends JsonDeserializer<String> {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final Pattern LETTER_PATTERN = Pattern.compile(".*[A-Za-z].*");

    public EncryptIfTaxCodeDeserializer() {
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();

        if (value == null || value.isBlank()) {
            return null;
        }

        UserRegistryConnector userRegistryConnector = SpringContext.getBean(UserRegistryConnector.class);

        if (!UUID_PATTERN.matcher(value).matches() && LETTER_PATTERN.matcher(value).matches()) {
            User user = userRegistryConnector.getUserByFiscalCode(value);
            return user != null ? user.getId() : null;
        }

        return value;
    }
}
