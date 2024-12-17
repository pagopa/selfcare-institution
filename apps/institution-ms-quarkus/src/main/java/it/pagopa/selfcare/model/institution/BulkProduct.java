package it.pagopa.selfcare.model.institution;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.pagopa.selfcare.constant.RelationshipState;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkProduct {

    @NotNull
    private String product;
    @NotNull
    private BillingResponse billing;
    private String pricingPlan;
    private RelationshipState status;
}
