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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
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
    InstitutionSendMailScheduledServiceImpl service;

    @Test
    void shouldSendMailToAllPecNotificationsForCurrentModuleDay() {
        List<PecNotification> notifications = getPecNotifications();
        PanacheMock.mock(PecNotification.class);
        ReactivePanacheQuery<ReactivePanacheMongoEntityBase> query = Mockito.mock(ReactivePanacheQuery.class);
        when(PecNotification.find(any(), any(Object.class)))
                .thenReturn(query);
        when(query.page(0, 1000)).thenReturn(query);

        ReactivePanacheQuery<ReactivePanacheMongoEntityBase> query2 = Mockito.mock(ReactivePanacheQuery.class);


        when(query.page(1, 1000)).thenReturn(query2);
        when(query.hasNextPage()).thenReturn(Uni.createFrom().item(true));
        when(query2.hasNextPage()).thenReturn(Uni.createFrom().item(false));
        when(query.list()).thenReturn(Uni.createFrom().item(List.of(notifications.get(0), notifications.get(1))));
        when(query2.list()).thenReturn(Uni.createFrom().item(List.of(notifications.get(2), notifications.get(3))));
        Product product = new Product();
        product.setTitle("prod-io");
        when(productService.getProduct("product-id")).thenReturn(product);

        Uni<Integer> result = service.retrieveInstitutionFromPecNotificationAndSendMail();
        UniAssertSubscriber<Integer> subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertItem(0).assertCompleted();
        Mockito.verify(mailService, Mockito.times(4))
                .sendMail(Mockito.anyList(), Mockito.anyString(), Mockito.anyMap());
    }

    private static List<PecNotification> getPecNotifications() {
        PecNotification notification1 = new PecNotification();
        PecNotification notification2 = new PecNotification();
        PecNotification notification3 = new PecNotification();
        PecNotification notification4 = new PecNotification();
        notification1.setProductId("product-id");
        notification1.setDigitalAddress("test@test1.it");
        notification1.setModuleDayOfTheEpoch(1);
        notification2.setProductId("product-id");
        notification2.setDigitalAddress("test@test2.it");
        notification2.setModuleDayOfTheEpoch(1);
        notification3.setProductId("product-id");
        notification3.setDigitalAddress("test@test3.it");
        notification3.setModuleDayOfTheEpoch(1);
        notification4.setProductId("product-id");
        notification4.setDigitalAddress("test@test4.it");
        notification4.setModuleDayOfTheEpoch(1);
        return List.of(notification1, notification2, notification3, notification4);
    }

    @Test
    void shouldLogErrorAndContinueOnMailSendFailure() {
        // Setup mocks
        PecNotification notification1 = new PecNotification();
        notification1.setProductId("product-id");
        notification1.setDigitalAddress("test@test.it");
        notification1.setModuleDayOfTheEpoch(1);
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
        Uni<Integer> result = service.retrieveInstitutionFromPecNotificationAndSendMail();
        UniAssertSubscriber<Integer> subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());

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
        Uni<Integer> result = service.retrieveInstitutionFromPecNotificationAndSendMail();
        UniAssertSubscriber<Integer> subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Verify
        Mockito.verify(mailService, Mockito.never()).sendMail(Mockito.anyList(), Mockito.anyString(), Mockito.anyMap());
        subscriber.assertItem(0).assertCompleted();
    }
}