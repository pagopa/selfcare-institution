package it.pagopa.selfcare.service;

import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.model.institution.Institution;

import java.util.List;

public interface InstitutionService {

    Uni<List<Institution>> retrieveInstitutionByIds(List<String> ids);

}
