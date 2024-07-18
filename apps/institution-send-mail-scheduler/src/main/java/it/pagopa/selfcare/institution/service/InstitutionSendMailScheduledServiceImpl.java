package it.pagopa.selfcare.institution.service;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import it.pagopa.selfcare.institution.config.ProductConfig;
import it.pagopa.selfcare.institution.entity.PecNotification;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.service.ProductService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.openapi.quarkus.selfcare_user_json.api.InstitutionApi;
import org.openapi.quarkus.selfcare_user_json.model.UserInstitutionResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class InstitutionSendMailScheduledServiceImpl implements InstitutionSendMailScheduledService {

    private static final Logger log = LoggerFactory.getLogger(InstitutionSendMailScheduledServiceImpl.class);

    private static final String PRODUCT_PLACEHOLDER = "nome_prodotto";
    private static final String USER_COUNT_PLACEHOLDER = "numero_utenti";

    private static final String ID_INSTITUTION_PLACEHOLDER = "id_institution";

    private static final String ID_PRODUCT_PLACEHOLDER = "id_prodotto";
    private final MailServiceImpl mailService;
    private final String templateMail;
    private final String templateMailFirstNotification;
    private final ProductService productService;
    private final String startDate;
    private final Integer querySize;

    private final ProductConfig productConfig;

    @RestClient
    @Inject
    private InstitutionApi userInstitutionApi;

    public InstitutionSendMailScheduledServiceImpl(MailServiceImpl mailService,
                                                   @ConfigProperty(name = "institution-send-mail.notification-path") String templateMail,
                                                   @ConfigProperty(name = "institution-send-mail.first-notification-path") String templateMailFirstNotification,
                                                   @ConfigProperty(name = "institution-send-mail.notification-start-date") String startDate,
                                                   ProductService productService,
                                                   @ConfigProperty(name = "institution-send-mail.notification-query-size") Integer querySize,
                                                   ProductConfig productConfig) {
        this.mailService = mailService;
        this.templateMail = templateMail;
        this.templateMailFirstNotification = templateMailFirstNotification;
        this.productService = productService;
        this.startDate = startDate;
        this.querySize = querySize;
        this.productConfig = productConfig;
    }

    @Override
    public Uni<Void> retrieveInstitutionFromPecNotificationAndSendMail(String productId) {
        Integer sendingFrequency = productConfig.getProductMap().getOrDefault(productId, productConfig.getProductMap().get("default"));
        Long moduleDayOfTheEpoch = calculateModuleDayOfTheEpoch(sendingFrequency);
        return retrieveFilteredAndPaginatedPecNotification(moduleDayOfTheEpoch, querySize, sendingFrequency, productId);
    }

    private Uni<Void> retrieveFilteredAndPaginatedPecNotification(Long moduleDayOfTheEpoch, int size, Integer sendingFrequency, String productId) {
        return Multi.createBy().repeating()
                .uni(AtomicInteger::new,
                        currentPage -> runQueryAndSendNotification(moduleDayOfTheEpoch, currentPage.getAndIncrement(), size, sendingFrequency, productId)
                                .onItem().invoke(() -> log.info("Page " + currentPage + " processed")))
                .until(Boolean.FALSE::equals)
                .collect()
                .asList()
                .replaceWith(Uni.createFrom().voidItem());

    }

    private Uni<Boolean> runQueryAndSendNotification(Long moduleDayOfTheEpoch, int page, int size, Integer sendingFrequency, String productId) {
        var pecNotificationPage = PecNotification.find(PecNotification.Fields.moduleDayOfTheEpoch.name() + "=?1 AND "
                        + PecNotification.Fields.productId.name() + "=?2", moduleDayOfTheEpoch, productId)
                .page(page, size);

        return pecNotificationPage.list()
                .onItem().transformToUni(reactivePanacheMongoEntityBases -> retrievePecNotificationListAndSendMail(reactivePanacheMongoEntityBases, sendingFrequency))
                .replaceWith(pecNotificationPage.hasNextPage())
                .onFailure().invoke(throwable -> log.error("Error during send scheduled mail", throwable));

    }

    private Uni<Void> retrievePecNotificationListAndSendMail(List<ReactivePanacheMongoEntityBase> query, Integer sendingFrequency) {
        return Multi.createFrom().iterable(query)
                .onItem().transform(entityBase -> (PecNotification) entityBase)
                .onItem().transformToUniAndMerge(pecNotification -> constructAndSendMail(pecNotification, sendingFrequency))
                .onFailure().invoke(throwable -> log.error("Error during send scheduled mail", throwable))
                .collect().asList()
                .replaceWith(Uni.createFrom().voidItem());
    }

    private Uni<Void> constructAndSendMail(PecNotification pecNotification, Integer sendingFrequency) {
        if (sendFirstMail(pecNotification, sendingFrequency)) {
            Product product = productService.getProduct(pecNotification.getProductId());
            Map<String, String> mailParameters = getMailParameters(pecNotification.getInstitutionId(), product, null);
            return mailService.sendMail(List.of(pecNotification.getDigitalAddress()), templateMailFirstNotification, mailParameters)
                    .onItem().invoke(() -> log.info(String.format("Mail sent for institution %s and product %s", pecNotification.getInstitutionId(), pecNotification.getProductId())))
                    .onFailure().recoverWithNull();
        } else {
            return countNewUsers(pecNotification.getInstitutionId(), pecNotification.getProductId())
                    .onItem().transformToUni(usersCount -> {
                        Product product = productService.getProduct(pecNotification.getProductId());
                        Map<String, String> mailParameters = getMailParameters(pecNotification.getInstitutionId(), product, usersCount);
                        return mailService.sendMail(List.of(pecNotification.getDigitalAddress()), templateMail, mailParameters)
                                .onItem().invoke(() -> log.info(String.format("Mail sent for institution %s and product %s", pecNotification.getInstitutionId(), pecNotification.getProductId())))
                                .onFailure().recoverWithNull();
                    });
        }
    }

    private static Map<String, String> getMailParameters(String institutionId, Product product, Integer users) {
        Map<String, String> mailParameters = new HashMap<>();
        mailParameters.put(PRODUCT_PLACEHOLDER, product.getTitle());
        mailParameters.put(ID_INSTITUTION_PLACEHOLDER, institutionId);
        mailParameters.put(ID_PRODUCT_PLACEHOLDER, product.getId());
        if (users != null) {
            mailParameters.put(USER_COUNT_PLACEHOLDER, users.toString());
        }
        return mailParameters;
    }

    private Uni<Integer> countNewUsers(String institutionId, String productId) {
        return userInstitutionApi.institutionsInstitutionIdUserInstitutionsGet(
                        institutionId,
                        null,
                        List.of(productId),
                        null,
                        null,
                        null)
                .onItem().transform(userInstitutionResponses -> filterOnCreatedAt(userInstitutionResponses, productId))
                .onItem().transformToUni(userInstitutionResponses -> Uni.createFrom().item(userInstitutionResponses.size()));
    }

    private List<UserInstitutionResponse> filterOnCreatedAt(List<UserInstitutionResponse> userInstitutionResponses, String productId){
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

       return userInstitutionResponses.stream()
                .filter(userInstitutionResponse -> productHasValidCreatedAt(productId, userInstitutionResponse, thirtyDaysAgo))
                .toList();
    }

    private static boolean productHasValidCreatedAt(String productId, UserInstitutionResponse userInstitutionResponse, LocalDateTime thirtyDaysAgo) {
        return !userInstitutionResponse.getProducts().stream()
                .filter(onboardedProductResponse -> onboardedProductResponse.getProductId().equals(productId) &&
                        onboardedProductResponse.getCreatedAt().isAfter(thirtyDaysAgo))
                .toList().isEmpty();
    }

    private Long calculateModuleDayOfTheEpoch(Integer sendingFrequency) {
        LocalDate date = LocalDate.parse(startDate);
        LocalDate now = LocalDate.now();
        long dayDifference = ChronoUnit.DAYS.between(date, now);
        return dayDifference % sendingFrequency;
    }


    private boolean sendFirstMail(PecNotification pecNotification, Integer sendingFrequency) {
        Instant date = pecNotification.getCreatedAt();
        Instant now = Instant.now();
        long dayDifference = ChronoUnit.DAYS.between(date, now);
        return dayDifference < sendingFrequency;
    }
}
