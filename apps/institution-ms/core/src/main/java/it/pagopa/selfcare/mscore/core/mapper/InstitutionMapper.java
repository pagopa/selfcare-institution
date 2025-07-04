package it.pagopa.selfcare.mscore.core.mapper;

import it.pagopa.selfcare.mscore.model.institution.Institution;
import it.pagopa.selfcare.mscore.model.institution.InstitutionProxyInfo;
import it.pagopa.selfcare.mscore.model.institution.NationalRegistriesProfessionalAddress;
import it.pagopa.selfcare.mscore.model.institution.Onboarding;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface InstitutionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "origin", ignore = true)
    Institution fromInstitutionProxyInfo(InstitutionProxyInfo proxyInfo);

    Institution fromProfessionalAddress(NationalRegistriesProfessionalAddress nationalRegistriesProfessionalAddress);

    @Mapping(target = "institutionType", expression = "java(setInstitutionType(institution, productId))")
    @Mapping(target = "origin", expression = "java(setOrigin(institution, productId))")
    @Mapping(target = "originId", expression = "java(setOriginId(institution, productId))")
    Institution toInstitutionFiltered(Institution institution, String productId);

    @Named("setInstitutionType")
    default InstitutionType setInstitutionType(Institution institution, String productId) {
        return findOnboardingByProductId(institution, productId)
                .map(Onboarding::getInstitutionType)
                .orElse(institution.getInstitutionType());
    }

    @Named("setOrigin")
    default String setOrigin(Institution institution, String productId) {
        return findOnboardingByProductId(institution, productId)
                .map(Onboarding::getOrigin)
                .orElse(institution.getOrigin());
    }

    @Named("setOriginId")
    default String setOriginId(Institution institution, String productId) {
        return findOnboardingByProductId(institution, productId)
                .map(Onboarding::getOriginId)
                .orElse(institution.getOriginId());
    }

    private Optional<Onboarding> findOnboardingByProductId(Institution institution, String productId) {
        return Optional.ofNullable(productId)
                .flatMap(id -> Optional.ofNullable(institution.getOnboarding())
                        .flatMap(onboardings -> onboardings.stream()
                                .filter(ob -> id.equals(ob.getProductId()))
                                .findFirst()));
    }
}
