package it.pagopa.selfcare.institution.service;

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
import static org.mockito.ArgumentMatchers.eq;

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
        PecNotification notification1 = new PecNotification();
        PecNotification notification2 = new PecNotification();
        notification1.setProductId("product-id");
        notification1.setInstitutionMail("test@test.it");
        notification1.setModuleDayOfTheEpoch(1);
        notification2.setProductId("product-id");
        notification2.setInstitutionMail("test@test.it");
        notification2.setModuleDayOfTheEpoch(1);
        List<PecNotification> notifications = List.of(notification1, notification2);
        Mockito.when(pecNotificationsRepository.list(any(), any(Object.class))).thenReturn(notifications);
        Product product = new Product();
        product.setTitle("prod-io");
        Mockito.when(productService.getProduct("product-id")).thenReturn(product);

        Uni<Integer> result = service.retrieveInstitutionFromPecNotificationAndSendMail();
        UniAssertSubscriber<Integer> subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertItem(0).assertCompleted();
        Mockito.verify(mailService, Mockito.times(2))
                .sendMailWithFile(eq(List.of("test@test.it")), Mockito.anyString(), Mockito.anyMap(), Mockito.anyString(), Mockito.isNull());
    }

    @Test
    void shouldLogErrorAndContinueOnMailSendFailure() {
        // Setup mocks
        PecNotification notification1 = new PecNotification();
        notification1.setProductId("product-id");
        notification1.setInstitutionMail("test@test.it");
        notification1.setModuleDayOfTheEpoch(1);
        Mockito.when(pecNotificationsRepository.list(any(), any(Object.class))).thenReturn(List.of(notification1));
        Product product = new Product();
        product.setTitle("prod-io");
        Mockito.when(productService.getProduct("product-id")).thenReturn(product);
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
        Mockito.when(pecNotificationsRepository.list(Mockito.anyString(), Mockito.anyLong())).thenReturn(Collections.emptyList());

        // Execute
        Uni<Integer> result = service.retrieveInstitutionFromPecNotificationAndSendMail();
        UniAssertSubscriber<Integer> subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());

        // Verify
        Mockito.verify(mailService, Mockito.never()).sendMailWithFile(Mockito.anyList(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyString(), Mockito.isNull());
        subscriber.assertItem(0).assertCompleted();
    }
}