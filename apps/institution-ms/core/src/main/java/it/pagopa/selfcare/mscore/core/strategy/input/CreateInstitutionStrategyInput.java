package it.pagopa.selfcare.mscore.core.strategy.input;

import it.pagopa.selfcare.onboarding.common.InstitutionType;
import it.pagopa.selfcare.mscore.core.util.InstitutionPaSubunitType;
import it.pagopa.selfcare.mscore.model.institution.InstitutionGeographicTaxonomies;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class CreateInstitutionStrategyInput {

    private String taxCode;
    private String ivassCode;
    private String description;
    private InstitutionPaSubunitType subunitType;
    private List<InstitutionGeographicTaxonomies> geographicTaxonomies;
    private String subunitCode;
    private InstitutionType institutionType;
    private String supportEmail;
    private String supportPhone;
    private String istatCode;
}
