package it.pagopa.selfcare.mscore.web.model.institution;

import it.pagopa.selfcare.mscore.web.util.EncryptIfTaxCode;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class PdaInstitutionRequest {

    @NotEmpty(message = "InjectionInstitutionType is required")
    private String injectionInstitutionType;

    @NotEmpty(message = "TaxCode is required")
    @EncryptIfTaxCode
    private String taxCode;

    private String description;

    private String istatCode;

    private BillingRequest billing;
}
