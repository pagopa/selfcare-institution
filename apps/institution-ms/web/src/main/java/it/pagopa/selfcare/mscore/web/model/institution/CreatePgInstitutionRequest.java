package it.pagopa.selfcare.mscore.web.model.institution;

import it.pagopa.selfcare.mscore.web.util.EncryptIfTaxCode;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class CreatePgInstitutionRequest {

    @NotEmpty(message = "taxId is required")
    @EncryptIfTaxCode
    private String taxId;

    private String description;

    private String istatCode;

    @NotNull
    private boolean existsInRegistry;

}
