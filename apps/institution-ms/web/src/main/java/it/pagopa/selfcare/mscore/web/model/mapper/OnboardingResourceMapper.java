package it.pagopa.selfcare.mscore.web.model.mapper;


import it.pagopa.selfcare.mscore.model.institution.Onboarding;
import it.pagopa.selfcare.mscore.model.onboarding.OnboardingRequest;
import it.pagopa.selfcare.mscore.web.model.institution.InstitutionOnboardingRequest;
import it.pagopa.selfcare.mscore.web.model.institution.OnboardingPut;
import it.pagopa.selfcare.mscore.web.model.institution.OnboardingResponse;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Optional;

@Mapper(componentModel = "spring", uses = {InstitutionUpdateMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OnboardingResourceMapper {

    OnboardingResponse toResponse(Onboarding onboarding);

    @Named("mapSignContract")
    default Boolean mapSignContract(Boolean signContract) {
        return Optional.ofNullable(signContract).orElse(true);
    }

    @Mapping(target = "contract", source = "contractPath")
    @Mapping(target = "createdAt", source = "activatedAt")
    Onboarding toOnboarding(InstitutionOnboardingRequest onboardingRequest);

    @Mapping(target = "contract", source = "contract.path")
    Onboarding toOnboarding(OnboardingRequest onboardingRequest);

    @Mapping(target = "billing.vatNumber", source = "vatNumber")
    Onboarding toOnboarding(OnboardingPut onboardingPut);
}
