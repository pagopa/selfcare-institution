package it.pagopa.selfcare.mscore.core;

import it.pagopa.selfcare.mscore.api.InstitutionConnector;
import it.pagopa.selfcare.mscore.api.PecNotificationConnector;
import it.pagopa.selfcare.mscore.api.ProductConnector;
import it.pagopa.selfcare.mscore.config.InstitutionSendMailConfig;
import it.pagopa.selfcare.mscore.constant.RelationshipState;
import it.pagopa.selfcare.mscore.core.mapper.TokenMapper;
import it.pagopa.selfcare.mscore.core.util.UtilEnumList;
import it.pagopa.selfcare.mscore.exception.InvalidRequestException;
import it.pagopa.selfcare.mscore.exception.ResourceNotFoundException;
import it.pagopa.selfcare.mscore.model.institution.Billing;
import it.pagopa.selfcare.mscore.model.institution.Institution;
import it.pagopa.selfcare.mscore.model.institution.Onboarding;
import it.pagopa.selfcare.mscore.model.onboarding.Token;
import it.pagopa.selfcare.mscore.model.onboarding.VerifyOnboardingFilters;
import it.pagopa.selfcare.mscore.model.pecnotification.PecNotification;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static it.pagopa.selfcare.mscore.constant.GenericError.DELETE_NOTIFICATION_OPERATION_ERROR;
import static it.pagopa.selfcare.mscore.constant.GenericError.ONBOARDING_OPERATION_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {OnboardingServiceImpl.class})
@ExtendWith(MockitoExtension.class)
class OnboardingServiceImplTest {

    @Mock
    private OnboardingDao onboardingDao;

    @InjectMocks
    private OnboardingServiceImpl onboardingServiceImpl;

    @Mock
    private InstitutionService institutionService;

    @Mock
    private InstitutionConnector institutionConnector;

    @Mock
    private ProductConnector productConnector;

    @Mock
    private UserNotificationService userNotificationService;

    @Mock
    private PecNotificationConnector pecNotificationConnector;

    @Spy
    private TokenMapper tokenMapper;

    @Mock
    private InstitutionSendMailConfig institutionSendMailConfig;

    /**
     * Method under test: {@link OnboardingServiceImpl#verifyOnboardingInfo(String, String)}
     */
    @Test
    void testVerifyOnboardingInfo() {
        doNothing().when(institutionService)
                .retrieveInstitutionsWithFilter(any(), any(), any());
        onboardingServiceImpl.verifyOnboardingInfo("42", "42");
        verify(institutionService).retrieveInstitutionsWithFilter(any(), any(),
                any());
    }

    /**
     * Method under test: {@link OnboardingServiceImpl#verifyOnboardingInfoSubunit(String, String, String)}
     */
    @Test
    void shouldNothingWhenVerifyOnboardingInfoSubunit() {
        when(institutionConnector.existsByTaxCodeAndSubunitCodeAndProductAndStatusList(any(), any(), any(), any()))
                .thenReturn(true);
        onboardingServiceImpl.verifyOnboardingInfoSubunit("42", "42", "example");
        verify(institutionConnector).existsByTaxCodeAndSubunitCodeAndProductAndStatusList(any(), any(), any(), any());
    }

    @Test
    void VerifyOnboardingInfoSubunitResourceNotFound() {
        when(institutionConnector.existsByTaxCodeAndSubunitCodeAndProductAndStatusList(any(), any(), any(), any()))
                .thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> onboardingServiceImpl.verifyOnboardingInfoSubunit("42", "42", "example"));

    }

    @Test
    void testVerifyOnboardingInfoByFilter() {
        // Arrange
        when(institutionConnector.existsOnboardingByFilters(Mockito.any())).thenReturn(true);

        VerifyOnboardingFilters verifyOnboardingFilters = new VerifyOnboardingFilters("Product", "", "", "Origin", "OriginId", "");
        // Act
        onboardingServiceImpl.verifyOnboardingInfoByFilters(verifyOnboardingFilters);

        // Assert that nothing has changed
        verify(institutionConnector).existsOnboardingByFilters(Mockito.any());
    }

    @Test
    void testVerifyOnboardingInfoByFilterNotFound() {
        // Arrange
        when(institutionConnector.existsOnboardingByFilters(Mockito.any())).thenReturn(false);

        VerifyOnboardingFilters verifyOnboardingFilters = new VerifyOnboardingFilters("Product", "", "", "Origin", "OriginId", "");
        // Act
        Assertions.assertThrows(ResourceNotFoundException.class, () -> onboardingServiceImpl.verifyOnboardingInfoByFilters(verifyOnboardingFilters));

        // Assert that nothing has changed
        verify(institutionConnector).existsOnboardingByFilters(Mockito.any());
    }

    /**
     * Method under test: {@link OnboardingServiceImpl#verifyOnboardingInfo(String, String)}
     */
    @Test
    void testVerifyOnboardingInfo5() {
        doThrow(new InvalidRequestException("An error occurred", "Code")).when(institutionService)
                .retrieveInstitutionsWithFilter(any(), any(), any());
        assertThrows(InvalidRequestException.class, () -> onboardingServiceImpl.verifyOnboardingInfo("42", "42"));
        verify(institutionService).retrieveInstitutionsWithFilter(any(), any(),
                any());
    }

    @Test
    void persistOnboarding_whenUserExistsOnRegistry() {

        Onboarding onboarding = dummyOnboarding();
        onboarding.setStatus(UtilEnumList.VALID_RELATIONSHIP_STATES.get(0));
        Institution institution = new Institution();
        institution.setId("institutionId");
        institution.setOnboarding(List.of(onboarding, dummyOnboarding()));
        institution.setInstitutionType(InstitutionType.PA.name());

        doNothing().when(pecNotificationConnector).insertPecNotification(any(PecNotification.class));
        when(institutionConnector.findById(institution.getId())).thenReturn(institution);
        when(institutionSendMailConfig.getPecNotificationDisabled()).thenReturn(false);
        when(institutionSendMailConfig.getProducts()).thenReturn(Map.of(onboarding.getProductId(),30));
        when(institutionSendMailConfig.getEpochDatePecNotification()).thenReturn("2024-01-01");

        String institutionId = institution.getId();

        String productId = onboarding.getProductId();
        Onboarding onb = new Onboarding();
        
        StringBuilder statusCode = new StringBuilder();

        onboardingServiceImpl.persistOnboarding(institutionId,
                productId, onb, statusCode);
        
        assertEquals(HttpStatus.OK.value(), Integer.parseInt(statusCode.toString()));
    }

    @Test
    void persistOnboarding_whenInstitutionTypeIsPT() {

        Onboarding onboarding = dummyOnboarding();
        onboarding.setStatus(UtilEnumList.VALID_RELATIONSHIP_STATES.get(0));
        Institution institution = new Institution();
        institution.setId("institutionId");
        institution.setInstitutionType(InstitutionType.PT.name());
        institution.setOnboarding(List.of(onboarding, dummyOnboarding()));

        when(institutionConnector.findById(institution.getId())).thenReturn(institution);

        String institutionId = institution.getId();
        String productId = onboarding.getProductId();
        Onboarding onb = new Onboarding();
        StringBuilder statusCode = new StringBuilder();

        onboardingServiceImpl.persistOnboarding(institutionId, productId, onb, statusCode);

        verify(pecNotificationConnector, never()).insertPecNotification(any(PecNotification.class));
        assertEquals(HttpStatus.OK.value(), Integer.parseInt(statusCode.toString()));
    }

    @Test
    void persistOnboarding_whenPecNotificationIsDisabled() {

        Onboarding onboarding = dummyOnboarding();
        onboarding.setStatus(UtilEnumList.VALID_RELATIONSHIP_STATES.get(0));
        Institution institution = new Institution();
        institution.setId("institutionId");
        institution.setOnboarding(List.of(onboarding, dummyOnboarding()));
        institution.setInstitutionType(InstitutionType.PG.name());

        when(institutionConnector.findById(institution.getId())).thenReturn(institution);
        when(institutionSendMailConfig.getPecNotificationDisabled()).thenReturn(true);

        String institutionId = institution.getId();

        String productId = onboarding.getProductId();
        Onboarding onb = new Onboarding();

        StringBuilder statusCode = new StringBuilder();

        onboardingServiceImpl.persistOnboarding(institutionId,
                productId, onb, statusCode);

        verify(pecNotificationConnector, never()).insertPecNotification(any(PecNotification.class));

        assertEquals(HttpStatus.OK.value(), Integer.parseInt(statusCode.toString()));
    }



    /**
     * Method under test: {@link OnboardingServiceImpl#persistOnboarding(String, String, Onboarding, StringBuilder)}
     */
    @Test
    void persistOnboarding_shouldRollback() {

        String pricingPlan = "pricingPlan";
        String productId = "productId";
        Onboarding onboarding = dummyOnboarding();
        onboarding.setStatus(UtilEnumList.VALID_RELATIONSHIP_STATES.get(0));

        Onboarding onboardingToPersist = new Onboarding();
        onboardingToPersist.setPricingPlan(pricingPlan);
        onboardingToPersist.setProductId(productId);
        onboardingToPersist.setBilling(new Billing());

        Institution institution = new Institution();
        institution.setId("institutionId");
        institution.setOnboarding(List.of(onboarding));


        when(institutionConnector.findById(institution.getId())).thenReturn(institution);
        when(institutionConnector.findAndUpdate(any(), any(), any(), any())).thenThrow(new RuntimeException());
        String institutionId = institution.getId();

        Assertions.assertThrows(InvalidRequestException.class, () -> onboardingServiceImpl.persistOnboarding(
        		institutionId, productId, onboardingToPersist, new StringBuilder()));

        verify(onboardingDao, times(1))
                .rollbackPersistOnboarding(any(), any());
    }

    /**
     * Method under test: {@link OnboardingServiceImpl#persistOnboarding(String, String, Onboarding, StringBuilder)}
     */
    @Test
    void persistOnboarding_whenUserNotExistsOnRegistry() {

        String pricingPlan = "pricingPlan";
        String productId = "productId";
        Billing billing = new Billing();
        billing.setVatNumber("vatNumber");
        billing.setPublicServices(false);
        billing.setRecipientCode("recipientCode");
        billing.setTaxCodeInvoicing("taxCodeInvoicing");
        Onboarding onboarding = dummyOnboarding();
        onboarding.setStatus(UtilEnumList.VALID_RELATIONSHIP_STATES.get(0));

        Onboarding onboardingToPersist = new Onboarding();
        onboardingToPersist.setPricingPlan(pricingPlan);
        onboardingToPersist.setProductId(productId);
        onboardingToPersist.setBilling(billing);
        onboardingToPersist.setIsAggregator(true);

        Institution institution = new Institution();
        institution.setId("institutionId");
        institution.setOnboarding(List.of(onboarding));
        institution.setDigitalAddress("test@junit.pagopa");
        institution.setInstitutionType(InstitutionType.PG.name());

        Token token = new Token();
        token.setId(onboarding.getTokenId());
        token.setInstitutionId("institutionId");
        token.setProductId(productId);
        token.setCreatedAt(onboarding.getCreatedAt());
        token.setUpdatedAt(onboarding.getUpdatedAt());
        token.setStatus(onboarding.getStatus());
        token.setContractSigned(onboarding.getContract());

        doNothing().when(pecNotificationConnector).insertPecNotification(any(PecNotification.class));
        when(institutionConnector.findById(institution.getId())).thenReturn(institution);
        when(institutionConnector.findAndUpdate(any(), any(), any(), any())).thenReturn(institution);
        when(institutionSendMailConfig.getPecNotificationDisabled()).thenReturn(false);
        when(institutionSendMailConfig.getProducts()).thenReturn(Map.of(productId,30));
        when(institutionSendMailConfig.getEpochDatePecNotification()).thenReturn("2024-01-01");
        
        StringBuilder statusCode = new StringBuilder();

        onboardingServiceImpl.persistOnboarding(institution.getId(), productId, onboardingToPersist, statusCode);

        ArgumentCaptor<Onboarding> captor = ArgumentCaptor.forClass(Onboarding.class);
        verify(institutionConnector, times(1))
                .findAndUpdate(any(), captor.capture(), any(), any());
        Onboarding actual = captor.getValue();
        assertEquals(billing, actual.getBilling());
        assertEquals(actual.getCreatedAt().getDayOfYear(), LocalDate.now().getDayOfYear());
        assertEquals(HttpStatus.CREATED.value(), Integer.parseInt(statusCode.toString()));

        ArgumentCaptor< PecNotification > argCaptor = ArgumentCaptor.forClass(PecNotification.class);
        verify(pecNotificationConnector, times(1)). insertPecNotification(argCaptor.capture());
        assertEquals(productId, argCaptor.getValue().getProductId());
        assertEquals("institutionId", argCaptor.getValue().getInstitutionId());
        assertEquals("test@junit.pagopa", argCaptor.getValue().getDigitalAddress());
    }

    private Onboarding dummyOnboarding() {
        Onboarding onboarding = new Onboarding();
        onboarding.setBilling(new Billing());
        onboarding.setTokenId("42");
        onboarding.setPricingPlan("C3");
        onboarding.setProductId("42");
        return onboarding;
    }

    @Test
    void deleteOnboardedInstitution_success() {

        String institutionId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();

        Onboarding onboarding = new Onboarding();
        onboarding.setProductId(productId);
        onboarding.setStatus(RelationshipState.DELETED);

        when(pecNotificationConnector.findAndDeletePecNotification(institutionId, productId)).thenReturn(true);
        
        onboardingServiceImpl.deleteOnboardedInstitution(institutionId, productId);

        verify(institutionConnector, times(1)).findAndDeleteOnboarding(institutionId, productId);
        verify(pecNotificationConnector, times(1)).findAndDeletePecNotification(institutionId, productId);
    }

    @Test
    void deleteOnboardedInstitution_deletePecNotificationFails() {

        String institutionId = UUID.randomUUID().toString();
        String productId = "prod-io";

        Onboarding onboarding = new Onboarding();
        onboarding.setProductId(productId);
        onboarding.setStatus(RelationshipState.DELETED);
        
        when(pecNotificationConnector.findAndDeletePecNotification(institutionId, productId)).thenReturn(false);

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            onboardingServiceImpl.deleteOnboardedInstitution(institutionId, productId);
        });

        assertEquals(DELETE_NOTIFICATION_OPERATION_ERROR.getMessage(), exception.getMessage());
        assertEquals(ONBOARDING_OPERATION_ERROR.getCode(), exception.getCode());

        verify(institutionConnector, times(1)).findAndDeleteOnboarding(institutionId, productId);
        verify(pecNotificationConnector, times(1)).findAndDeletePecNotification(institutionId, productId);
    }



    @Test
    public void insertPecNotification() {
        String institutionId = "testInstitution";
        String productId = "testProduct";
        String digitalAddress = "test@domain.com";
        OffsetDateTime createdAtOnboarding = OffsetDateTime.of(2024, 8, 30, 10, 0, 0, 0, ZoneOffset.UTC);

        Map<String, Integer> products = new HashMap<>();
        products.put(productId, 30);

        when(institutionSendMailConfig.getPecNotificationDisabled()).thenReturn(false);
        when(institutionSendMailConfig.getProducts()).thenReturn(products);
        when(institutionSendMailConfig.getEpochDatePecNotification()).thenReturn("2024-01-01");
        doNothing().when(pecNotificationConnector).insertPecNotification(any(PecNotification.class));

        // Act
        onboardingServiceImpl.insertPecNotification(institutionId, productId, digitalAddress, createdAtOnboarding);

        // Assert
        ArgumentCaptor<PecNotification> argumentCaptor = ArgumentCaptor.forClass(PecNotification.class);
        verify(pecNotificationConnector, times(1)).insertPecNotification(argumentCaptor.capture());
        assertEquals(2, argumentCaptor.getValue().getModuleDayOfTheEpoch());
    }

    @Test
    public void testCalculateModuleDayOfTheEpoch() {
        OffsetDateTime mockCurrentDate = OffsetDateTime.of(2024, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC); // 31 days after epoch

        int result = onboardingServiceImpl.calculateModuleDayOfTheEpoch("2024-01-01", mockCurrentDate, 30);

        LocalDate epochStart = LocalDate.parse("2024-01-01");
        long daysDiff = ChronoUnit.DAYS.between(epochStart, mockCurrentDate);
        int expected = (int) (daysDiff % 30);

        assertEquals(expected, result);
    }
}

