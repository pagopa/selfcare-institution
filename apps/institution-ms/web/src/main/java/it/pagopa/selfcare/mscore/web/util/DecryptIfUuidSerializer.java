package it.pagopa.selfcare.mscore.web.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.pagopa.selfcare.mscore.api.UserRegistryConnector;
import it.pagopa.selfcare.mscore.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class DecryptIfUuidSerializer extends JsonSerializer<String> {

    private final UserRegistryConnector userRegistryConnector;

    @Autowired
    public DecryptIfUuidSerializer(UserRegistryConnector userRegistryConnector) {
        this.userRegistryConnector = userRegistryConnector;
    }

    public DecryptIfUuidSerializer() {
        this.userRegistryConnector = null;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isBlank()) {
            gen.writeNull();
            return;
        }

        if (isUuid(value) && userRegistryConnector != null) {
            User user = userRegistryConnector.getUserByInternalId(value);
            gen.writeString(user != null ? user.getFiscalCode() : null);
        } else {
            gen.writeString(value);
        }
    }

    private boolean isUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

