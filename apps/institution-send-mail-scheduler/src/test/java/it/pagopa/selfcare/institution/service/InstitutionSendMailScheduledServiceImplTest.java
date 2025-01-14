package it.pagopa.selfcare.institution.service;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import it.pagopa.selfcare.institution.config.ProductConfig;
import it.pagopa.selfcare.institution.entity.PecNotification;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.service.ProductService;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapi.quarkus.selfcare_user_json.api.InstitutionApi;
import org.openapi.quarkus.selfcare_user_json.model.OnboardedProductResponse;
import org.openapi.quarkus.selfcare_user_json.model.UserInstitutionResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class InstitutionSendMailScheduledServiceImplTest {
    @InjectMock
    MailServiceImpl mailService;

    @InjectMock
    ProductService productService;

    @Inject
    ProductConfig productConfig;

    @Inject
    InstitutionSendMailScheduledServiceImpl service;

    @RestClient
    @InjectMock
    InstitutionApi institutionApi;

    @ConfigProperty(name = "institution-send-mail.notification-query-size")
    Integer querySize;

    @ConfigProperty(name = "institution-send-mail.first-notification-path")
    String templateMailFirstNotification;

    @ConfigProperty(name = "institution-send-mail.notification-path")
    String templateMail;

    @Test
    void sendMailToAllPecNotificationsForCurrentModuleDay() {
        String institutionId = "institution-id";
        String productId = "product-io";
        List<PecNotification> notifications = getPecNotifications();
        PanacheMock.mock(PecNotification.class);
        ReactivePanacheQuery<ReactivePanacheMongoEntityBase> query = Mockito.mock(ReactivePanacheQuery.class);
        when(PecNotification.find(any(), any(Object.class)))
                .thenReturn(query);
        when(query.page(0, querySize)).thenReturn(query);


        ReactivePanacheQuery<ReactivePanacheMongoEntityBase> query2 = Mockito.mock(ReactivePanacheQuery.class);
        when(query.page(1, querySize)).thenReturn(query2);
        when(query.hasNextPage()).thenReturn(Uni.createFrom().item(true));
        when(query.firstResult()).thenReturn(Uni.createFrom().item(notifications.get(0)));


        when(query2.firstResult()).thenReturn(Uni.createFrom().item(notifications.get(2)));
        when(query2.hasNextPage()).thenReturn(Uni.createFrom().item(false));

        Product product = new Product();
        product.setTitle("prod-io");
        when(productService.getProduct("product-io")).thenReturn(product);

        when(institutionApi.institutionsInstitutionIdUserInstitutionsGet(institutionId,
                null,
                List.of(productId),
                null,
                null,
                null)).thenReturn(Uni.createFrom().item(getUserInstitutionResponse(institutionId, productId)));

        Uni<Void> result = service.retrieveInstitutionFromPecNotificationAndSendMail();
        UniAssertSubscriber<Void> subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertNotTerminated();
        Mockito.verify(mailService, Mockito.atLeast(4))
                .sendMail(Mockito.anyList(), Mockito.anyString(), Mockito.anyMap(), Mockito.any());
    }


    @Test
    void shouldNotSendMail_whenOnboardingHappenToday() {
        PecNotification notification1 = new PecNotification();
        notification1.setProductId("product-id");
        notification1.setDigitalAddress("test@test.it");
        notification1.setModuleDayOfTheEpoch(1);
        notification1.setCreatedAt(Instant.now().minusSeconds(360));
        notification1.setInstitutionId("institution-id");
        PanacheMock.mock(PecNotification.class);

        ReactivePanacheQuery<ReactivePanacheMongoEntityBase> query = Mockito.mock(ReactivePanacheQuery.class);
        when(PecNotification.find(any(), any(Object.class)))
                .thenReturn(query);
        when(query.page(0, querySize)).thenReturn(query);


        when(query.hasNextPage()).thenReturn(Uni.createFrom().item(false));
        when(query.firstResult()).thenReturn(Uni.createFrom().item(notification1));

        Uni<Void> result = service.retrieveInstitutionFromPecNotificationAndSendMail();
        UniAssertSubscriber<Void> subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted();
        Mockito.verify(mailService, Mockito.times(0))
                .sendMail(Mockito.anyList(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyString());
    }

    @Test
    void shouldLogErrorAndContinueOnMailSendFailure() {
        final int page = 0;
        final long moduleDayOfTheEpoch = 1L;
        // Setup mocks
        PecNotification notification1 = new PecNotification();
        notification1.setProductId("product-id");
        notification1.setDigitalAddress("test@test.it");
        notification1.setModuleDayOfTheEpoch(1);
        notification1.setCreatedAt(Instant.now().minusSeconds(86500));
        notification1.setInstitutionId("institution-id");
        PanacheMock.mock(PecNotification.class);
        ReactivePanacheQuery<ReactivePanacheMongoEntityBase> query = Mockito.mock(ReactivePanacheQuery.class);
        when(PecNotification.find(any(), any(Object.class)))
                .thenReturn(query);
        when(query.page(0, querySize)).thenReturn(query);
        when(query.hasNextPage()).thenReturn(Uni.createFrom().item(false));
        when(query.firstResult()).thenReturn(Uni.createFrom().item(notification1));
        Product product = new Product();
        product.setTitle("prod-io");
        when(productService.getProduct("product-id")).thenReturn(product);
        Mockito.doThrow(new RuntimeException("Mail send failed")).when(mailService).sendMail(Mockito.anyList(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyString());

        // Execute
        Uni<Boolean> result = service.runQueryAndSendNotification(moduleDayOfTheEpoch, page, "productId");
        UniAssertSubscriber<Boolean> subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Verify
        subscriber.assertFailedWith(RuntimeException.class, "Mail send failed");
    }

    @Test
    void shouldHandleNoPecNotificationsForCurrentModuleDay() {
        final int page = 0;
        PanacheMock.mock(PecNotification.class);
        ReactivePanacheQuery<ReactivePanacheMongoEntityBase> query = Mockito.mock(ReactivePanacheQuery.class);
        when(PecNotification.find(any(), any(Object.class)))
                .thenReturn(query);
        when(query.page(page, querySize)).thenReturn(query);
        when(query.hasNextPage()).thenReturn(Uni.createFrom().item(false));
        when(query.firstResult()).thenReturn(Uni.createFrom().nullItem());

        // Execute
        Uni<Boolean> result = service.runQueryAndSendNotification(0L, page, "productId");
        UniAssertSubscriber<Boolean> subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Verify
        Mockito.verify(mailService, Mockito.never()).sendMail(Mockito.anyList(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyString());
        subscriber.assertCompleted();
    }

    @Test
    void sendFirstMailTemplate() {
        String institutionId = "institution-id";
        String productId = "prod-io";

        PecNotification pecNotification = new PecNotification();
        pecNotification.setProductId(productId);
        pecNotification.setInstitutionId(institutionId);
        pecNotification.setDigitalAddress("test@test1.it");
        pecNotification.setCreatedAt(Instant.now().minus(productConfig.products().get(productId), ChronoUnit.DAYS));
        pecNotification.setModuleDayOfTheEpoch(1);

        PanacheMock.mock(PecNotification.class);
        ReactivePanacheQuery<ReactivePanacheMongoEntityBase> query = Mockito.mock(ReactivePanacheQuery.class);
        when(PecNotification.find(any(), any(Object.class))).thenReturn(query);
        when(query.page(0, querySize)).thenReturn(query);
        when(query.hasNextPage()).thenReturn(Uni.createFrom().item(true));
        when(query.firstResult()).thenReturn(Uni.createFrom().item(pecNotification));

        Product product = new Product();
        product.setTitle("prod-io");
        when(productService.getProduct("prod-io")).thenReturn(product);

        when(institutionApi.institutionsInstitutionIdUserInstitutionsGet(institutionId,
                null,
                List.of(productId),
                null,
                null,
                null)).thenReturn(Uni.createFrom().item(getUserInstitutionResponse(institutionId, productId)));

        Uni<Void> result = service.retrieveInstitutionFromPecNotificationAndSendMail();
        UniAssertSubscriber<Void> subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertNotTerminated();
        Mockito.verify(mailService, Mockito.atLeast(1))
                .sendMail(Mockito.anyList(), Mockito.eq(templateMailFirstNotification), Mockito.anyMap(), Mockito.any());
        Mockito.verify(mailService, Mockito.never())
                .sendMail(Mockito.anyList(), Mockito.eq(templateMail), Mockito.anyMap(), Mockito.any());
    }

    private List<UserInstitutionResponse> getUserInstitutionResponse(String institutionId, String productId) {
        UserInstitutionResponse userInstitutionResponse = new UserInstitutionResponse();
        userInstitutionResponse.setInstitutionId(institutionId);
        OnboardedProductResponse userProductResponse = new OnboardedProductResponse();
        userProductResponse.setCreatedAt(LocalDateTime.now());
        userProductResponse.setProductId(productId);
        userInstitutionResponse.setProducts(List.of(userProductResponse));
        return List.of(userInstitutionResponse);
    }

    private static List<PecNotification> getPecNotifications() {
        PecNotification notification1 = new PecNotification();
        PecNotification notification2 = new PecNotification();
        PecNotification notification3 = new PecNotification();
        PecNotification notification4 = new PecNotification();
        notification1.setProductId("product-id");
        notification1.setInstitutionId("institution-id");
        notification1.setDigitalAddress("test@test1.it");
        notification1.setCreatedAt(Instant.now().minus(60, ChronoUnit.DAYS));
        notification1.setModuleDayOfTheEpoch(1);
        notification2.setProductId("product-id");
        notification2.setDigitalAddress("test@test2.it");
        notification2.setModuleDayOfTheEpoch(1);
        notification2.setCreatedAt(Instant.now());
        notification2.setInstitutionId("institution-id");
        notification3.setProductId("product-id");
        notification3.setDigitalAddress("test@test3.it");
        notification3.setModuleDayOfTheEpoch(1);
        notification3.setInstitutionId("institution-id");
        notification3.setCreatedAt(Instant.now());
        notification4.setProductId("product-id");
        notification4.setDigitalAddress("test@test4.it");
        notification4.setModuleDayOfTheEpoch(1);
        notification4.setCreatedAt(Instant.now());
        notification4.setInstitutionId("institution-id");
        return List.of(notification1, notification2, notification3, notification4);
    }
}