package it.pagopa.selfcare.institution.service;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import it.pagopa.selfcare.institution.entity.PecNotification;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.service.ProductService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class InstitutionSendMailScheduledServiceImpl implements InstitutionSendMailScheduledService {

    private static final Logger log = LoggerFactory.getLogger(InstitutionSendMailScheduledServiceImpl.class);

    private static final String PRODUCT_PLACEHOLDER = "nome_prodotto";
    private final MailServiceImpl mailService;
    private final String templateMail;
    private final ProductService productService;
    private final String startDate;
    private final Integer sendingFrequency;
    private final Integer querySize;

    public InstitutionSendMailScheduledServiceImpl(MailServiceImpl mailService,
                                                   @ConfigProperty(name = "institution-send-mail.notification-path") String templateMail,
                                                   @ConfigProperty(name = "institution-send-mail.notification-sending-frequency") Integer sendingFrequency,
                                                   @ConfigProperty(name = "institution-send-mail.notification-start-date") String startDate,
                                                   ProductService productService,
                                                   @ConfigProperty(name = "institution-send-mail.notification-query-size") Integer querySize) {
        this.mailService = mailService;
        this.templateMail = templateMail;
        this.productService = productService;
        this.startDate = startDate;
        this.sendingFrequency = sendingFrequency;
        this.querySize = querySize;
    }

    @Override
    public Uni<Integer> retrieveInstitutionFromPecNotificationAndSendMail() {
        Long moduleDayOfTheEpoch = calculateModuleDayOfTheEpoch();
        int startPage = 0;
        return retrieveFilteredAndPaginatedPecNotification(moduleDayOfTheEpoch, startPage, querySize);
    }

    public Uni<Integer> retrieveFilteredAndPaginatedPecNotification(Long moduleDayOfTheEpoch, int page, int size) {
        log.info("Retrieving page " + page + " of PecNotification");
        return Multi.createBy().repeating()
                .uni(AtomicInteger::new,
                        currentPage -> runQueryAndSendNotification(moduleDayOfTheEpoch, currentPage.getAndIncrement(), size)
                                .onItem().invoke(() -> log.info("Page " + currentPage + " processed")))
                .until(Boolean.FALSE::equals)
                .collect()
                .asList()
                .flatMap(ignored -> Uni.createFrom().item(0));

    }

    public Uni<Boolean> runQueryAndSendNotification(Long moduleDayOfTheEpoch, int page, int size) {
        var pecNotificationPage = PecNotification.find(PecNotification.Fields.moduleDayOfTheEpoch.name(), moduleDayOfTheEpoch)
                .page(page, size);

        return pecNotificationPage.list()
                .onItem().transformToUni(this::retrievePecNotificationListAndSendMail)
                .replaceWith(pecNotificationPage.hasNextPage())
                .onFailure().invoke(throwable -> log.error("Error during send scheduled mail", throwable));

    }

    private Uni<Void> retrievePecNotificationListAndSendMail(List<ReactivePanacheMongoEntityBase> query) {
        return Multi.createFrom().iterable(query)
                .onItem().transform(entityBase -> (PecNotification) entityBase)
                .onItem().transformToUniAndMerge(this::constructAndSendMail)
                .onFailure().invoke(throwable -> log.error("Error during send scheduled mail", throwable))
                .collect().asList()
                .replaceWith(Uni.createFrom().voidItem());
    }

    private Uni<Void> constructAndSendMail(PecNotification pecNotification) {
        Product product = productService.getProduct(pecNotification.getProductId());
        Map<String, String> mailParameters = Map.of(PRODUCT_PLACEHOLDER, product.getTitle());
        return mailService.sendMail(List.of(pecNotification.getDigitalAddress()), templateMail, mailParameters)
                .onItem().invoke(() -> log.info(String.format("Mail sent for institution %s and product %s", pecNotification.getInstitutionId(), pecNotification.getProductId())))
                .onFailure().recoverWithNull();
    }

    private Long calculateModuleDayOfTheEpoch() {
        LocalDate date = LocalDate.parse(startDate);
        LocalDate now = LocalDate.now();
        long dayDifference = ChronoUnit.DAYS.between(date, now);
        return dayDifference % sendingFrequency;
    }
}
