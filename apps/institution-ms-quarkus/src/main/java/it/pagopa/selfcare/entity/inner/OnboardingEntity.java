package it.pagopa.selfcare.entity.inner;

import it.pagopa.selfcare.constant.RelationshipState;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.OffsetDateTime;

@Data
@FieldNameConstants(asEnum = true)
public class OnboardingEntity {

    private String productId;
    private RelationshipState status;
    private String tokenId;
    private String contract;
    private String pricingPlan;
    private BillingEntity billing;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime closedAt;
    private Boolean isAggregator;

}
