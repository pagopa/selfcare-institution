package it.pagopa.selfcare.model.mapper;


import it.pagopa.selfcare.entity.InstitutionEntity;
import it.pagopa.selfcare.model.institution.Institution;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "cdi", imports = UUID.class)
public interface InstitutionEntityMapper {

    Institution convertToInstitution(InstitutionEntity entity);

}
