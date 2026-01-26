package it.pagopa.selfcare.mscore.web.model.institution;

import it.pagopa.selfcare.mscore.core.util.InstitutionPaSubunitType;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class InstitutionFromIpaPost {

    @NotNull
    private String taxCode;
    private String subunitCode;
    private InstitutionPaSubunitType subunitType;
    private List<GeoTaxonomies> geographicTaxonomies;
    private InstitutionType institutionType;
    private String supportEmail;
    private String supportPhone;
    private String rea;
    private String shareCapital;
    private String businessRegisterPlace;
}
