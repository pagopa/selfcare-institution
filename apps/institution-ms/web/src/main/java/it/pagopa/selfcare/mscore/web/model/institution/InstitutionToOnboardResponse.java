package it.pagopa.selfcare.mscore.web.model.institution;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.pagopa.selfcare.mscore.web.util.DecryptIfUuid;
import lombok.Data;

@Data
public class InstitutionToOnboardResponse {

    @JsonProperty("cfImpresa")
    @DecryptIfUuid
    private String id;

    @JsonProperty("denominazione")
    private String description;
}
