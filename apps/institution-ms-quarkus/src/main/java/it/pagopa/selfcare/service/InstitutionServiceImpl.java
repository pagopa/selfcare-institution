package it.pagopa.selfcare.service;

import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.model.institution.Institution;
import it.pagopa.selfcare.model.mapper.InstitutionEntityMapper;
import it.pagopa.selfcare.repository.InstitutionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@ApplicationScoped
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionRepository repository;

    private final InstitutionEntityMapper mapper;

    @Override
    public Uni<List<Institution>> retrieveInstitutionByIds(List<String> ids) {
        return repository.findAllById(ids).onItem().transform(l -> l.stream()
                .map(mapper::convertToInstitution).collect(Collectors.toList()));
    }

}
