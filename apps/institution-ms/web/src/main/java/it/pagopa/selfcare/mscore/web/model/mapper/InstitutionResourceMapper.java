package it.pagopa.selfcare.mscore.web.model.mapper;


import it.pagopa.selfcare.mscore.model.institution.Institution;
import it.pagopa.selfcare.mscore.model.institution.InstitutionGeographicTaxonomies;
import it.pagopa.selfcare.mscore.model.institution.InstitutionUpdate;
import it.pagopa.selfcare.mscore.web.model.institution.*;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring",  uses = {OnboardingResourceMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface InstitutionResourceMapper {

    @Mapping(target = "aooParentCode", source = "paAttributes.aooParentCode")
    @Mapping(target = "rootParent", source = ".", qualifiedByName = "setRootParent")
    InstitutionResponse toInstitutionResponse(Institution institution);

    @Mapping(target = "aooParentCode", source = "institution.paAttributes.aooParentCode")
    @Mapping(target = "rootParent", source = "institution", qualifiedByName = "setRootParent")
    @Mapping(target = "institutionType", expression = "java(setInstitutionType(institution, productId))")
    @Mapping(target = "origin", expression = "java(setOrigin(institution, productId))")
    @Mapping(target = "originId", expression = "java(setOriginId(institution, productId))")
    InstitutionResponse toInstitutionResponseWithType(Institution institution, String productId);

    @Named("setRootParent")
    static RootParentResponse setRootParent(Institution institution) {
        if(StringUtils.hasText(institution.getRootParentId())){
            RootParentResponse rootParentResponse = new RootParentResponse();
            rootParentResponse.setId(institution.getRootParentId());
            rootParentResponse.setDescription(institution.getParentDescription());
            return rootParentResponse;
        }
        return null;
    }

    @Named("setInstitutionType")
    default String setInstitutionType(Institution institution, String productId) {
        return Optional.ofNullable(productId)
                .filter(id -> !institution.getOnboarding().isEmpty())
                .map(id -> institution.getOnboarding().get(0).getInstitutionType().name())
                .orElse(institution.getInstitutionType().name());
    }

    @Named("setOrigin")
    default String setOrigin(Institution institution, String productId) {
        return Optional.ofNullable(productId)
                .filter(id -> !institution.getOnboarding().isEmpty())
                .map(id -> institution.getOnboarding().get(0).getOrigin())
                .orElse(institution.getOrigin());
    }

    @Named("setOriginId")
    default String setOriginId(Institution institution, String productId) {
        return Optional.ofNullable(productId)
                .filter(id -> !institution.getOnboarding().isEmpty())
                .map(id -> institution.getOnboarding().get(0).getOriginId())
                .orElse(institution.getOriginId());
    }

    InstitutionGeographicTaxonomies toInstitutionGeographicTaxonomies(GeoTaxonomies geoTaxonomies);

    @Mapping(target = "geographicTaxonomies", source = ".", qualifiedByName = "toGeographicTaxonomies")
    InstitutionUpdate toInstitutionUpdate(InstitutionPut institutionPut);

    @Named("toGeographicTaxonomies")
    static List<InstitutionGeographicTaxonomies> toGeographicTaxonomies(InstitutionPut institution) {
        return Optional.ofNullable(institution.getGeographicTaxonomyCodes())
                .map(geoTaxonomiesCodes -> geoTaxonomiesCodes.stream()
                        .map(code -> new InstitutionGeographicTaxonomies(code, null))
                        .toList())
                .orElse(null);
    }

    Institution toInstitution(InstitutionRequest request);

    Institution toInstitution(PdaInstitutionRequest request);
}
