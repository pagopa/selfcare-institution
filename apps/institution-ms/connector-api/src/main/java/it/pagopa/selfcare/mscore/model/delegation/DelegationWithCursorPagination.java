package it.pagopa.selfcare.mscore.model.delegation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DelegationWithCursorPagination {

    private List<Delegation> delegations;
    private Long cursor;

}
