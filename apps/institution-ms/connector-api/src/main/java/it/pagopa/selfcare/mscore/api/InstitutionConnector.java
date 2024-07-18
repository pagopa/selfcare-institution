package it.pagopa.selfcare.mscore.api;

import it.pagopa.selfcare.commons.base.utils.InstitutionType;
import it.pagopa.selfcare.mscore.constant.RelationshipState;
import it.pagopa.selfcare.mscore.constant.SearchMode;
import it.pagopa.selfcare.mscore.model.institution.*;
import it.pagopa.selfcare.mscore.model.onboarding.VerifyOnboardingFilters;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface InstitutionConnector {

    Institution save(Institution example);

    List<Institution> findAll();

    void deleteById(String id);

    void findAndDeleteOnboarding(String institutionId, String productId);

    List<Institution> findByTaxCodeSubunitCodeAndOrigin(String taxtCode, String subunitCode, String origin, String originId);

    Boolean existsByTaxCodeAndSubunitCodeAndProductAndStatusList(String taxtCode, String subunitCode, Optional<String> productId, List<RelationshipState> validRelationshipStates);

    Optional<Institution> findByExternalId(String externalId);

    List<Institution> findWithFilter(String externalId, String productId, List<RelationshipState> validRelationshipStates);

    Institution findById(String id);

    Institution findAndUpdateStatus(String id, String tokenId, RelationshipState state);

    Institution findAndUpdate(String id, Onboarding onboarding, List<InstitutionGeographicTaxonomies> geographicTaxonomies, InstitutionUpdate institutionUpdate);

    Institution findByExternalIdAndProductId(String externalId, String productId);

    List<Onboarding> findOnboardingByIdAndProductId(String externalId, String productId);

    List<Institution> findInstitutionsByProductId(String productId, Integer page, Integer size);

    void findAndRemoveOnboarding(String institutionId, Onboarding onboarding);

    List<Institution> findByGeotaxonomies(List<String> geo, SearchMode searchMode);

    List<Institution> findByProductId(String productId);

    List<Institution> findAllByIds(List<String> ids);

    List<String> findByExternalIdsAndProductId(List<ValidInstitution> externalIds, String productId);

    Institution updateOnboardedProductCreatedAt(String institutionId, String productId, OffsetDateTime createdAt);

    List<Institution> findBrokers(String productId, InstitutionType type);

    List<Institution> findByTaxCodeAndSubunitCode(String taxCode, String subunitCode);

    List<Institution> findByOriginAndOriginId(String origin, String originId);

    Boolean existsOnboardingByFilters(VerifyOnboardingFilters filters);
}
