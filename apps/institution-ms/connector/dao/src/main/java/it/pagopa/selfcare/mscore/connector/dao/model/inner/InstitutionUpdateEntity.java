package it.pagopa.selfcare.mscore.connector.dao.model.inner;

import it.pagopa.selfcare.onboarding.common.InstitutionType;
import lombok.Data;

import java.util.List;

@Data
public class InstitutionUpdateEntity {

    private InstitutionType institutionType;
    private String description;
    private String digitalAddress;
    private String address;
    private String taxCode;
    private String zipCode;
    private String city;
    private String county;
    private String country;
    private PaymentServiceProviderEntity paymentServiceProvider;
    private DataProtectionOfficerEntity dataProtectionOfficer;
    private List<GeoTaxonomyEntity> geographicTaxonomies;
    private String rea;
    private String shareCapital;
    private String businessRegisterPlace;
    private String supportEmail;
    private String supportPhone;
    private boolean imported;
    private AdditionalInformationsEntity additionalInformations;
}
