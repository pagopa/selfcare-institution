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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
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

    private final boolean sendAllNotification;
    private final ProductConfig productConfig;

    @RestClient
    @Inject
    InstitutionApi userInstitutionApi;

    public InstitutionSendMailScheduledServiceImpl(MailServiceImpl mailService,
                                                   @ConfigProperty(name = "institution-send-mail.notification-path") String templateMail,
                                                   @ConfigProperty(name = "institution-send-mail.first-notification-path") String templateMailFirstNotification,
                                                   @ConfigProperty(name = "institution-send-mail.notification-start-date") String startDate,
                                                   ProductService productService,
                                                   @ConfigProperty(name = "institution-send-mail.notification-query-size") Integer querySize,
                                                   @ConfigProperty(name = "institution-send-mail.notification-send-all") boolean sendAllNotification,
                                                   ProductConfig productConfig) {
        this.mailService = mailService;
        this.templateMail = templateMail;
        this.templateMailFirstNotification = templateMailFirstNotification;
        this.productService = productService;
        this.startDate = startDate;
        this.querySize = querySize;
        this.sendAllNotification = sendAllNotification;
        this.productConfig = productConfig;
    }

    @Override
    public Uni<Void> retrieveInstitutionFromPecNotificationAndSendMail() {

        return Multi.createFrom().iterable(productConfig.products().keySet())
                .onItem().transformToUniAndMerge(productId ->
                        retrieveFilteredAndPaginatedPecNotification(calculateModuleDayOfTheEpoch(productConfig.products().get(productId)), productId))
                .toUni().replaceWithVoid();
    }

    private Uni<Void> retrieveFilteredAndPaginatedPecNotification(Long moduleDayOfTheEpoch, String productId) {
        return Multi.createBy().repeating()
                .uni(AtomicInteger::new,
                        currentPage -> runQueryAndSendNotification(moduleDayOfTheEpoch, currentPage.getAndIncrement(), productId))
                .withDelay(Duration.ofSeconds(30))
                .until(Boolean.FALSE::equals)
                .collect()
                .asList()
                .replaceWith(Uni.createFrom().voidItem());

    }

    public Uni<Boolean> runQueryAndSendNotification(Long moduleDayOfTheEpoch, int page, String productId) {
        var pecNotificationPage = PecNotification.find(PecNotification.Fields.moduleDayOfTheEpoch.name() + "=?1 AND "
                        + PecNotification.Fields.productId.name() + "=?2", moduleDayOfTheEpoch, productId)
                .page(page, querySize);

        Uni<List<ReactivePanacheMongoEntityBase>> uniResultEntity = sendAllNotification
                ? pecNotificationPage.list()
                : pecNotificationPage.firstResult()
                    .onItem().ifNotNull().transform(List::of)
                    .onItem().ifNull().continueWith(ArrayList::new);

        return uniResultEntity
                .onItem().invoke(pecNotifications -> log.info(String.format("[%s, moduleDayOfTheEpoch=%d] Page %d processed, iteration has been found %d pec notifications", productId, moduleDayOfTheEpoch, page, pecNotifications.size())))
                .onItem().transformToUni(this::retrievePecNotificationListAndSendMail)
                .onItem().invoke(mailSize -> log.info(String.format("[%s, moduleDayOfTheEpoch=%d] Page %d processed, iteration has been send %d mails", productId, moduleDayOfTheEpoch, page, mailSize)))
                .replaceWith(pecNotificationPage.hasNextPage())
                .onFailure().invoke(throwable -> log.error(String.format("[%s, moduleDayOfTheEpoch=%d] Error during send mail page %d processed, error=%s", productId, moduleDayOfTheEpoch, page, throwable.getMessage())));

    }

    private Uni<Integer> retrievePecNotificationListAndSendMail(List<ReactivePanacheMongoEntityBase> query) {
        return Multi.createFrom().iterable(query)
                .onItem().transform(entityBase -> (PecNotification) entityBase)
                .onItem().transformToUniAndMerge(this::constructAndSendMail)
                .onFailure().invoke(throwable -> log.error("Error during send scheduled mail", throwable))
                .collect().asList()
                .map(List::size);
    }

    private Uni<Void> constructAndSendMail(PecNotification pecNotification) {

        long dayDifference = ChronoUnit.DAYS.between(pecNotification.getCreatedAt(), Instant.now());
        //Onboarding happened today, we must not send mail
        if(dayDifference <= 0) {
            return Uni.createFrom().voidItem();
        }
        if(Objects.isNull(pecNotification.getDigitalAddress())) {
            log.warn(String.format("Mail did not send for institution %s and product %s because digitalAddress is empty!", pecNotification.getInstitutionId(), pecNotification.getProductId()));
            return Uni.createFrom().voidItem();
        }

        if (sendFirstMail(dayDifference, pecNotification.getProductId())) {
            Product product = productService.getProduct(pecNotification.getProductId());
            String productTitle = Optional.ofNullable(product).map(Product::getTitle).orElse(null);
            Map<String, String> mailParameters = getMailParameters(pecNotification.getInstitutionId(), product, null);
            return mailService.sendMail(List.of(pecNotification.getDigitalAddress()), templateMailFirstNotification, mailParameters, productTitle)
                    .onItem().invoke(() -> log.info(String.format("Mail sent for institution %s and product %s", pecNotification.getInstitutionId(), pecNotification.getProductId())))
                    .onFailure().recoverWithNull();
        } else {
            return countNewUsers(pecNotification.getInstitutionId(), pecNotification.getProductId())
                    .onItem().transformToUni(usersCount -> {
                        Product product = productService.getProduct(pecNotification.getProductId());
                        String productTitle = Optional.ofNullable(product).map(Product::getTitle).orElse(null);
                        Map<String, String> mailParameters = getMailParameters(pecNotification.getInstitutionId(), product, usersCount);
                        return mailService.sendMail(List.of(pecNotification.getDigitalAddress()), templateMail, mailParameters, productTitle)
                                .onItem().invoke(() -> log.info(String.format("Mail sent for institution %s and product %s", pecNotification.getInstitutionId(), pecNotification.getProductId())))
                                .onFailure().recoverWithNull();
                    });
        }
    }

    private static Map<String, String> getMailParameters(String institutionId, Product product, Integer users) {
        Map<String, String> mailParameters = new HashMap<>();
        mailParameters.put(PRODUCT_PLACEHOLDER, Optional.ofNullable(product).map(Product::getTitle).orElse(null));
        mailParameters.put(ID_INSTITUTION_PLACEHOLDER, institutionId);
        mailParameters.put(ID_PRODUCT_PLACEHOLDER, Optional.ofNullable(product).map(Product::getId).orElse(null));
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

    private boolean sendFirstMail(long dayDifference, String productId) {
        return dayDifference <= productConfig.products().getOrDefault(productId, 0);
    }
}
