package it.pagopa.selfcare.mscore.web.model.institution;

import it.pagopa.selfcare.mscore.core.util.InstitutionPaSubunitType;
import it.pagopa.selfcare.mscore.web.util.EncryptIfTaxCode;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class InstitutionFromIpaPost {

    @NotNull
    @EncryptIfTaxCode
    private String taxCode;
    private String subunitCode;
    private InstitutionPaSubunitType subunitType;
    private List<GeoTaxonomies> geographicTaxonomies;
    private InstitutionType institutionType;
    private String supportEmail;
    private String supportPhone;
}
