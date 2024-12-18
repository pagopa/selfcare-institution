package it.pagopa.selfcare.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepositoryBase;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.entity.InstitutionEntity;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@ApplicationScoped
public class InstitutionRepository implements ReactivePanacheMongoRepositoryBase<InstitutionEntity, String> {

    public Uni<List<InstitutionEntity>> findAllById(List<String> ids) {
        return list("_id in ?1", ids);
    }

}
