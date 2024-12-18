package it.pagopa.selfcare.controller;

import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.model.institution.BulkInstitutions;
import it.pagopa.selfcare.model.institution.BulkPartiesSeed;
import it.pagopa.selfcare.model.institution.Institution;
import it.pagopa.selfcare.model.mapper.InstitutionMapperCustom;
import it.pagopa.selfcare.service.InstitutionService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

@Path("/")
public class ManagementController {

    private final InstitutionService institutionService;

    public ManagementController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @POST
    @Path("/bulk/institutions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<BulkInstitutions> retrieveInstitutionByIds(@Valid BulkPartiesSeed bulkPartiesSeed) {
        List<String> ids = new ArrayList<>(bulkPartiesSeed.getPartyIdentifiers());
        Uni<List<Institution>> institution = institutionService.retrieveInstitutionByIds(ids);
        return institution.onItem().transform(l -> InstitutionMapperCustom.toBulkInstitutions(l, ids));
    }

}
