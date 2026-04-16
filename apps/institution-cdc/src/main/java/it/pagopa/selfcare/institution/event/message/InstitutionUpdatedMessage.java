package it.pagopa.selfcare.institution.event.message;

import lombok.Data;

@Data
public class InstitutionUpdatedMessage {

    private String eventType = "InstitutionUpdated";
    private String publisherId;
    private String institutionId;
    private String institutionDescription;

}
