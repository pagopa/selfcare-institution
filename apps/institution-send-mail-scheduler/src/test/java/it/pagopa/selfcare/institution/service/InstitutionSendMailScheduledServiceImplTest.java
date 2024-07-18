package it.pagopa.selfcare.institution.service;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import it.pagopa.selfcare.institution.entity.PecNotification;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.service.ProductService;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapi.quarkus.selfcare_user_json.api.InstitutionApi;
import org.openapi.quarkus.selfcare_user_json.model.OnboardedProductResponse;
import org.openapi.quarkus.selfcare_user_json.model.UserInstitutionResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class InstitutionSendMailScheduledServiceImplTest {

    public static final int PAGE_SIZE = 1000;
    @InjectMock
    MailServiceImpl mailService;

    @InjectMock
    ProductService productService;

    @Inject
    InstitutionSendMailScheduledServiceImpl service;

    @RestClient
    @InjectMock
    InstitutionApi institutionApi;

    @Test
    void sendMailToAllPecNotificationsForCurrentModuleDay() {
        String institutionId = "institution-id";
        String productId = "product-io";
        List<PecNotification> notifications = getPecNotifications();
        PanacheMock.mock(PecNotification.class);
        ReactivePanacheQuery<ReactivePanacheMongoEntityBase> query = Mockito.mock(ReactivePanacheQuery.class);
        when(PecNotification.find(any(), any(Object.class)))
                .thenReturn(query);
        when(query.page(0, PAGE_SIZE)).thenReturn(query);


        ReactivePanacheQuery<ReactivePanacheMongoEntityBase> query2 = Mockito.mock(ReactivePanacheQuery.class);
        when(query.page(1, PAGE_SIZE)).thenReturn(query2);
        when(query.hasNextPage()).thenReturn(Uni.createFrom().item(true));
        when(query.list()).thenReturn(Uni.createFrom().item(List.of(notifications.get(0), notifications.get(1))));


        when(query2.list()).thenReturn(Uni.createFrom().item(List.of(notifications.get(2), notifications.get(3))));
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
        subscriber.assertCompleted();
        Mockito.verify(mailService, Mockito.atLeast(4))
                .sendMail(Mockito.anyList(), Mockito.anyString(), Mockito.anyMap());
    }

    @Test
    void shouldLogErrorAndContinueOnMailSendFailure() {
        // Setup mocks
        PecNotification notification1 = new PecNotification();
        notification1.setProductId("product-id");
        notification1.setDigitalAddress("test@test.it");
        notification1.setModuleDayOfTheEpoch(1);
        notification1.setCreatedAt(Instant.now());
        notification1.setInstitutionId("institution-id");
        PanacheMock.mock(PecNotification.class);
        ReactivePanacheQuery<ReactivePanacheMongoEntityBase> query = Mockito.mock(ReactivePanacheQuery.class);
        when(PecNotification.find(any(), any(Object.class)))
                .thenReturn(query);
        when(query.page(0, 1000)).thenReturn(query);
        when(query.hasNextPage()).thenReturn(Uni.createFrom().item(false));
        when(query.list()).thenReturn(Uni.createFrom().item(List.of(notification1)));
        Product product = new Product();
        product.setTitle("prod-io");
        when(productService.getProduct("product-id")).thenReturn(product);
        Mockito.doThrow(new RuntimeException("Mail send failed")).when(mailService).sendMail(Mockito.anyList(), Mockito.anyString(), Mockito.anyMap());

        // Execute
        Uni<Void> result = service.retrieveInstitutionFromPecNotificationAndSendMail();
        UniAssertSubscriber<Void> subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Verify
        subscriber.assertFailedWith(RuntimeException.class, "Mail send failed");
    }

    @Test
    void shouldHandleNoPecNotificationsForCurrentModuleDay() {
        PanacheMock.mock(PecNotification.class);
        ReactivePanacheQuery<ReactivePanacheMongoEntityBase> query = Mockito.mock(ReactivePanacheQuery.class);
        when(PecNotification.find(any(), any(Object.class)))
                .thenReturn(query);
        when(query.page(0, 1000)).thenReturn(query);
        when(query.hasNextPage()).thenReturn(Uni.createFrom().item(false));
        when(query.list()).thenReturn(Uni.createFrom().item(Collections.emptyList()));

        // Execute
        Uni<Void> result = service.retrieveInstitutionFromPecNotificationAndSendMail();
        UniAssertSubscriber<Void> subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Verify
        Mockito.verify(mailService, Mockito.never()).sendMail(Mockito.anyList(), Mockito.anyString(), Mockito.anyMap());
        subscriber.assertCompleted();
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