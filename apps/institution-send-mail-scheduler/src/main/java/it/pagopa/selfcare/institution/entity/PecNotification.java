package it.pagopa.selfcare.institution.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;

import java.time.OffsetDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldNameConstants(asEnum = true)
@MongoEntity(collection="PecNotification")
public class PecNotification extends ReactivePanacheMongoEntity{

    private ObjectId id;
    private Integer moduleDayOfTheEpoch;
    private String productId;
    private String institutionId;
    private String digitalAddress;
    private OffsetDateTime createdAt;

}
