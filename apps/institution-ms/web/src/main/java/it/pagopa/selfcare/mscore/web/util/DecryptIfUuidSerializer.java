package it.pagopa.selfcare.mscore.web.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.pagopa.selfcare.mscore.api.UserRegistryConnector;
import it.pagopa.selfcare.mscore.exception.ResourceNotFoundException;
import it.pagopa.selfcare.mscore.model.user.User;

import java.io.IOException;
import java.util.regex.Pattern;

public class DecryptIfUuidSerializer extends JsonSerializer<String> {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public DecryptIfUuidSerializer() {
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isBlank()) {
            gen.writeNull();
            return;
        }

        UserRegistryConnector userRegistryConnector = SpringContext.getBean(UserRegistryConnector.class);

        if (UUID_PATTERN.matcher(value).matches()) {
            try {
                User user = userRegistryConnector.getUserByInternalIdWithFiscalCode(value);
                // If the user is not found in UserRegistry, return the original value
                gen.writeString(user != null ? user.getFiscalCode() : value);
            } catch (ResourceNotFoundException e) {
                // 404: user not found → return the original value
                gen.writeString(value);
            }
        } else {
            gen.writeString(value);
        }
    }
}
