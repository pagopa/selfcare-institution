package it.pagopa.selfcare.model.institution;

import lombok.Data;

@Data
public class BillingResponse {
    private String vatNumber;
    private String taxCodeInvoicing;
    private String recipientCode;
    private boolean publicServices;
}
