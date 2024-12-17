package it.pagopa.selfcare.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import it.pagopa.selfcare.constant.Origin;
import it.pagopa.selfcare.entity.inner.*;
import it.pagopa.selfcare.onboarding.common.InstitutionType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.bson.codecs.pojo.annotations.BsonId;

import java.time.OffsetDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@MongoEntity(collection = "Institution")
@FieldNameConstants(asEnum = true)
public class InstitutionEntity extends ReactivePanacheMongoEntityBase {

    @BsonId
    private String id;

    private String externalId;

    private Origin origin;
    private String originId;
    private String description;
    private InstitutionType institutionType;
    private String digitalAddress;
    private String address;
    private String zipCode;
    private String taxCode;
    private String city;
    private String county;
    private String country;
    private String istatCode;
    private BillingEntity billing;
    private List<OnboardingEntity> onboarding;
    private List<GeoTaxonomyEntity> geographicTaxonomies;
    private List<AttributesEntity> attributes;
    private PaymentServiceProviderEntity paymentServiceProvider;
    private DataProtectionOfficerEntity dataProtectionOfficer;
    private String rea;
    private String shareCapital;
    private String businessRegisterPlace;
    private String supportEmail;
    private String supportPhone;
    private boolean imported;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String subunitCode;
    private String subunitType;
    private String parentDescription;
    private String rootParentId;
    private PaAttributesEntity paAttributes;
    private boolean delegation;

}
