package it.pagopa.selfcare.institution.service;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import it.pagopa.selfcare.institution.entity.PecNotification;
import it.pagopa.selfcare.institution.repository.PecNotificationsRepository;
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
    PecNotificationsRepository pecNotificationsRepository;

    @InjectMock
    MailServiceImpl mailService;

    @InjectMock
    ProductService productService;

    @Inject
    InstitutionSendMailScheduledServiceImpl service;

    @Test
    void shouldSendMailToAllPecNotificationsForCurrentModuleDay() {
        List<PecNotification> notifications = getPecNotifications();
        PanacheQuery<PecNotification> query = Mockito.mock(PanacheQuery.class);
        PanacheQuery<PecNotification> query2 = Mockito.mock(PanacheQuery.class);
        when(pecNotificationsRepository.find(any(), any(Object.class))).thenReturn(query);
        when(query.hasNextPage()).thenReturn(true);
        when(query2.hasNextPage()).thenReturn(false);
        when(query.page(0, 500)).thenReturn(query);
        when(query.page(1, 500)).thenReturn(query2);
        when(query.list()).thenReturn(List.of(notifications.get(0), notifications.get(1)));
        when(query2.list()).thenReturn(List.of(notifications.get(2), notifications.get(3)));
        Product product = new Product();
        product.setTitle("prod-io");
        when(productService.getProduct("product-id")).thenReturn(product);

        Uni<Integer> result = service.retrieveInstitutionFromPecNotificationAndSendMail();
        UniAssertSubscriber<Integer> subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertItem(0).assertCompleted();
        Mockito.verify(mailService, Mockito.times(4))
                .sendMailWithFile(Mockito.anyList(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyString(), Mockito.isNull());
    }

    private static List<PecNotification> getPecNotifications() {
        PecNotification notification1 = new PecNotification();
        PecNotification notification2 = new PecNotification();
        PecNotification notification3 = new PecNotification();
        PecNotification notification4 = new PecNotification();
        notification1.setProductId("product-id");
        notification1.setInstitutionMail("test@test1.it");
        notification1.setModuleDayOfTheEpoch(1);
        notification2.setProductId("product-id");
        notification2.setInstitutionMail("test@test2.it");
        notification2.setModuleDayOfTheEpoch(1);
        notification3.setProductId("product-id");
        notification3.setInstitutionMail("test@test3.it");
        notification3.setModuleDayOfTheEpoch(1);
        notification4.setProductId("product-id");
        notification4.setInstitutionMail("test@test4.it");
        notification4.setModuleDayOfTheEpoch(1);
        return List.of(notification1, notification2, notification3, notification4);
    }

    @Test
    void shouldLogErrorAndContinueOnMailSendFailure() {
        // Setup mocks
        PecNotification notification1 = new PecNotification();
        notification1.setProductId("product-id");
        notification1.setInstitutionMail("test@test.it");
        notification1.setModuleDayOfTheEpoch(1);
        PanacheQuery<PecNotification> query = Mockito.mock(PanacheQuery.class);
        when(pecNotificationsRepository.find(any(), any(Object.class))).thenReturn(query);
        when(query.page(Mockito.anyInt(), Mockito.anyInt())).thenReturn(query);
        when(query.hasNextPage()).thenReturn(true);
        when(query.list()).thenReturn(List.of(notification1));
        Product product = new Product();
        product.setTitle("prod-io");
        when(productService.getProduct("product-id")).thenReturn(product);
        Mockito.doThrow(new RuntimeException("Mail send failed")).when(mailService).sendMailWithFile(Mockito.anyList(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyString(), Mockito.isNull());

        // Execute
        Uni<Integer> result = service.retrieveInstitutionFromPecNotificationAndSendMail();
        UniAssertSubscriber<Integer> subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Verify
        Mockito.verify(mailService).sendMailWithFile(Mockito.anyList(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyString(), Mockito.isNull());
        subscriber.assertFailedWith(RuntimeException.class, "Mail send failed");
    }

    @Test
    void shouldHandleNoPecNotificationsForCurrentModuleDay() {
        // Setup mocks
        PanacheQuery<PecNotification> query = Mockito.mock(PanacheQuery.class);
        when(pecNotificationsRepository.find(any(), any(Object.class))).thenReturn(query);
        when(query.page(Mockito.anyInt(), Mockito.anyInt())).thenReturn(query);
        when(query.hasNextPage()).thenReturn(false);
        when(query.list()).thenReturn(Collections.emptyList());

        // Execute
        Uni<Integer> result = service.retrieveInstitutionFromPecNotificationAndSendMail();
        UniAssertSubscriber<Integer> subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Verify
        Mockito.verify(mailService, Mockito.never()).sendMailWithFile(Mockito.anyList(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyString(), Mockito.isNull());
        subscriber.assertItem(0).assertCompleted();
    }
}