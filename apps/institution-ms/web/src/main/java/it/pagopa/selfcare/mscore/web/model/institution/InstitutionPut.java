package it.pagopa.selfcare.mscore.web.model.institution;

import lombok.Data;
import lombok.ToString;

import jakarta.validation.Valid;
import java.util.List;

@Data
@ToString
public class InstitutionPut {

    List<String> geographicTaxonomyCodes;
    private String digitalAddress;
    private String description;
    private String parentDescription;
    private String address;
    private String zipCode;
    private String rea;
    private String shareCapital;
    private String businessRegisterPlace;
    @Valid
    private List<OnboardingPut> onboardings;
}
