package it.pagopa.selfcare.mscore.web.model.institution;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
public class InstitutionOnboardingResponse {

    private String id;
    private String externalId;
    private String origin;
    private String originId;
    private String description;
    private String institutionType;
    private String digitalAddress;
    private String address;
    private String zipCode;
    private String taxCode;
    private Map<String, OnboardingResponse> onboardings;
    private List<GeoTaxonomies> geographicTaxonomies;
    private List<AttributesResponse> attributes;
    private PaymentServiceProviderResponse paymentServiceProvider;
    private DataProtectionOfficerResponse dataProtectionOfficer;
    private String rea;
    private String shareCapital;
    private String businessRegisterPlace;
    private String supportEmail;
    private String supportPhone;
    private boolean imported;
    private String subunitCode;
    private String subunitType;
    private String aooParentCode;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
