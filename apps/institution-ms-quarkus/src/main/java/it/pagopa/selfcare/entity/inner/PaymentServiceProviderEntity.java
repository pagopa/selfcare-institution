package it.pagopa.selfcare.entity.inner;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants(asEnum = true)
public class PaymentServiceProviderEntity {

    private String abiCode;
    private String businessRegisterNumber;
    private String legalRegisterName;
    private String legalRegisterNumber;
    private boolean vatNumberGroup;

}
