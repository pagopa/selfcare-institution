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
