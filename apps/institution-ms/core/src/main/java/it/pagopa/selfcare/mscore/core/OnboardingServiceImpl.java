package it.pagopa.selfcare.mscore.core;

import it.pagopa.selfcare.mscore.api.InstitutionConnector;
import it.pagopa.selfcare.mscore.api.PecNotificationConnector;
import it.pagopa.selfcare.mscore.config.InstitutionSendMailConfig;
import it.pagopa.selfcare.mscore.constant.CustomError;
import it.pagopa.selfcare.mscore.constant.RelationshipState;
import it.pagopa.selfcare.mscore.core.util.UtilEnumList;
import it.pagopa.selfcare.mscore.exception.InvalidRequestException;
import it.pagopa.selfcare.mscore.exception.ResourceNotFoundException;
import it.pagopa.selfcare.mscore.model.institution.Institution;
import it.pagopa.selfcare.mscore.model.institution.Onboarding;
import it.pagopa.selfcare.mscore.model.onboarding.VerifyOnboardingFilters;
import it.pagopa.selfcare.mscore.model.pecnotification.PecNotification;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static it.pagopa.selfcare.mscore.constant.GenericError.*;

@Slf4j
@Service
public class OnboardingServiceImpl implements OnboardingService {
    private final OnboardingDao onboardingDao;
    private final InstitutionService institutionService;
    private final InstitutionConnector institutionConnector;
    private final PecNotificationConnector pecNotificationConnector;

    private final InstitutionSendMailConfig institutionSendMailConfig;

    public OnboardingServiceImpl(OnboardingDao onboardingDao,
                                 InstitutionService institutionService,
                                 InstitutionConnector institutionConnector,
                                 PecNotificationConnector pecNotificationConnector,
                                 InstitutionSendMailConfig institutionSendMailConfig) {

        this.onboardingDao = onboardingDao;
        this.institutionService = institutionService;
        this.institutionConnector = institutionConnector;
        this.pecNotificationConnector = pecNotificationConnector;
        this.institutionSendMailConfig = institutionSendMailConfig;
    }

    @Override
    public void verifyOnboardingInfo(String externalId, String productId) {
        institutionService.retrieveInstitutionsWithFilter(externalId, productId, UtilEnumList.VALID_RELATIONSHIP_STATES);
    }

    @Override
    public void verifyOnboardingInfoSubunit(String taxCode, String subunitCode, String productId) {
        Boolean existsOnboardingValid = institutionConnector.existsByTaxCodeAndSubunitCodeAndProductAndStatusList(taxCode,
                subunitCode, Optional.ofNullable(productId), UtilEnumList.VALID_RELATIONSHIP_STATES);
        if (Boolean.FALSE.equals(existsOnboardingValid)) {
            throw new ResourceNotFoundException(String.format(CustomError.INSTITUTION_NOT_ONBOARDED.getMessage(), taxCode, productId),
                    CustomError.INSTITUTION_NOT_ONBOARDED.getCode());
        }
    }

    @Override
    public void verifyOnboardingInfoByFilters(VerifyOnboardingFilters filters) {
        filters.setValidRelationshipStates(UtilEnumList.VALID_RELATIONSHIP_STATES);

        Boolean existsOnboardingValid = institutionConnector.existsOnboardingByFilters(filters);
        if (Boolean.FALSE.equals(existsOnboardingValid)) {
            throw new ResourceNotFoundException(CustomError.INSTITUTION_NOT_ONBOARDED_BY_FILTERS.getMessage(),
                    CustomError.INSTITUTION_NOT_ONBOARDED_BY_FILTERS.getCode());
        }
    }

    public void insertPecNotification(String institutionId, String productId, String digitalAddress, OffsetDateTime createdAtOnboarding) {

        //If sending mail ActiveUsers enabled for this product
        if (!institutionSendMailConfig.getPecNotificationDisabled()
                && institutionSendMailConfig.getProducts().containsKey(productId)) {

            PecNotification pecNotification = new PecNotification();
            pecNotification.setId(ObjectId.get());
            pecNotification.setCreatedAt(Instant.now());
            pecNotification.setProductId(productId);
            pecNotification.setInstitutionId(institutionId);
            pecNotification.setModuleDayOfTheEpoch(calculateModuleDayOfTheEpoch(institutionSendMailConfig.getEpochDatePecNotification(),
                    createdAtOnboarding, institutionSendMailConfig.getProducts().get(productId)));
            pecNotification.setDigitalAddress(digitalAddress);

            pecNotificationConnector.insertPecNotification(pecNotification);
        }

    }

    public int calculateModuleDayOfTheEpoch(String epochDatePecNotification, OffsetDateTime createdAtOnboarding, Integer sendingFrequencyPecNotification) {
        LocalDate epochStart = LocalDate.parse(epochDatePecNotification);
        long daysDiff = ChronoUnit.DAYS.between(epochStart, createdAtOnboarding);
        return (int) (daysDiff % sendingFrequencyPecNotification);
    }

    @Override
    public Institution persistOnboarding(String institutionId, String
            productId, Onboarding onboarding, StringBuilder httpStatus) {

        Institution institution = persistAndGetInstitution(institutionId, productId, onboarding, httpStatus);
        if (!InstitutionType.PT.equals(institution.getInstitutionType())) {
            insertPecNotification(institutionId, productId, institution.getDigitalAddress(), onboarding.getCreatedAt());
        }
        return institution;
    }

    private Institution persistAndGetInstitution(String institutionId, String productId, Onboarding onboarding, StringBuilder httpStatus) {
        log.trace("persistForUpdate start");
        log.debug("persistForUpdate institutionId = {}, productId = {}", institutionId, productId);
        onboarding.setStatus(RelationshipState.ACTIVE);
        onboarding.setProductId(productId);

        if (Objects.isNull(onboarding.getCreatedAt())) {
            onboarding.setCreatedAt(OffsetDateTime.now());
        }

        //Verify if onboarding exists, in case onboarding must fail
        final Institution institution = institutionConnector.findById(institutionId);

        if (Optional.ofNullable(institution.getOnboarding()).flatMap(onboardings -> onboardings.stream()
                .filter(item -> item.getProductId().equals(productId) && UtilEnumList.VALID_RELATIONSHIP_STATES.contains(item.getStatus()))
                .findAny()).isPresent()) {

            httpStatus.append(HttpStatus.OK.value());
            return institution;
        }

        try {
            //If not exists, persist a new onboarding for product
            final Institution institutionUpdated = institutionConnector.findAndUpdate(institutionId, onboarding, List.of(), null);

            log.trace("persistForUpdate end");
            httpStatus.append(HttpStatus.CREATED.value());
            return institutionUpdated;
        } catch (Exception e) {
            onboardingDao.rollbackPersistOnboarding(institutionId, onboarding);
            log.info("rollbackPersistOnboarding completed for institution {} and product {}", institutionId, productId);
            throw new InvalidRequestException(ONBOARDING_OPERATION_ERROR.getMessage() + " " + e.getMessage(),
                    ONBOARDING_OPERATION_ERROR.getCode());
        }
    }

    @Override
    public void deleteOnboardedInstitution(String institutionId, String productId) {

        log.trace("persist logic delete Onboarding - start");
        institutionConnector.findAndDeleteOnboarding(institutionId, productId);
        log.trace("persist logic delete Onboarding - end");

        log.trace("persist delete PecNotification - start");
        boolean isDeleted = pecNotificationConnector.findAndDeletePecNotification(institutionId, productId);
        if (!isDeleted) {
            throw new InvalidRequestException(DELETE_NOTIFICATION_OPERATION_ERROR.getMessage(),
                    ONBOARDING_OPERATION_ERROR.getCode());
        }
        log.trace("persist delete PecNotification - end");

    }

}
