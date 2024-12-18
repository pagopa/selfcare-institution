package it.pagopa.selfcare.model.institution;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Valid
public class BulkPartiesSeed {

    @NotNull
    private List<String> partyIdentifiers;
}
