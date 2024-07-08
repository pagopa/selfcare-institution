package it.pagopa.selfcare.institution.service;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import it.pagopa.selfcare.institution.entity.PecNotification;
import it.pagopa.selfcare.institution.repository.PecNotificationsRepository;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.service.ProductService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class InstitutionSendMailScheduledServiceImpl implements InstitutionSendMailScheduledService{

    private static final Logger log = LoggerFactory.getLogger(InstitutionSendMailScheduledServiceImpl.class);

    private static final String PRODUCT_PLACEHOLDER = "nome_prodotto";
    private final PecNotificationsRepository pecNotificationsRepository;
    private final MailServiceImpl mailService;
    private final String templateMail;
    private final ProductService productService;
    private final String startDate;
    private final Integer sendingFrequency;

    public InstitutionSendMailScheduledServiceImpl(PecNotificationsRepository pecNotificationsRepository,
                                                   MailServiceImpl mailService,
                                                   @ConfigProperty(name = "institution-send-mail.notification-path") String templateMail,
                                                   @ConfigProperty(name = "institution-send-mail.notification-sending-frequency") Integer sendingFrequency,
                                                   @ConfigProperty(name = "institution-send-mail.notification-start-date") String startDate,
                                                   ProductService productService) {
        this.pecNotificationsRepository = pecNotificationsRepository;
        this.mailService = mailService;
        this.templateMail = templateMail;
        this.productService = productService;
        this.startDate = startDate;
        this.sendingFrequency = sendingFrequency;
    }

    @Override
    public Uni<Integer> retrieveInstitutionFromPecNotificationAndSendMail() {
        Long moduleDayOfTheEpoch = calculateModuleDayOfTheEpoch();
        return retrieveFilteredAndPaginatedPecNotification(moduleDayOfTheEpoch, 0, 500);
    }

    public Uni<Integer> retrieveFilteredAndPaginatedPecNotification(Long moduleDayOfTheEpoch, int page, int size) {
        log.info("Retrieving page " + page + " of PecNotification");
        PanacheQuery<PecNotification> query = pecNotificationsRepository.find("moduleDayOfTheEpoch", moduleDayOfTheEpoch)
                .page(page, size);
        if(query.hasNextPage()) {
            return retrievePecNotificationListAndSendMail(query)
                    .onItem().transformToUni(unused -> retrieveFilteredAndPaginatedPecNotification(moduleDayOfTheEpoch, page + 1, size))
                    .replaceWith(0);
        }else{
            return retrievePecNotificationListAndSendMail(query)
                    .replaceWith(0);
        }
    }

    private Uni<Void> retrievePecNotificationListAndSendMail(PanacheQuery<PecNotification> query) {
        return Multi.createFrom().iterable(query.list())
                .onItem().transformToUniAndMerge(this::constructAndSendMail)
                .onFailure().invoke(throwable -> log.error("Error during send scheduled mail", throwable))
                .collect().asList()
                .onItem().invoke(list -> log.info("Mail sent to institutions"))
                .replaceWith(Uni.createFrom().voidItem());
    }

    private Uni<Void> constructAndSendMail(PecNotification pecNotification) {
        Product product = productService.getProduct(pecNotification.getProductId());
        Map<String, String> mailParameters = retrieveMailParameters(product.getTitle());
        mailService.sendMailWithFile(List.of(pecNotification.getInstitutionMail()), templateMail, mailParameters, null, null);
        return Uni.createFrom().voidItem();
    }

    private Map<String, String> retrieveMailParameters(String productName) {
        Map<String, String> mailParameters = new HashMap<>();
        mailParameters.put(PRODUCT_PLACEHOLDER, productName);
        return mailParameters;
    }

    private Long calculateModuleDayOfTheEpoch() {
        LocalDate date = LocalDate.parse(startDate);
        LocalDate now = LocalDate.now();
        long dayDifference = ChronoUnit.DAYS.between(date, now);
        return dayDifference % sendingFrequency;
    }
}
