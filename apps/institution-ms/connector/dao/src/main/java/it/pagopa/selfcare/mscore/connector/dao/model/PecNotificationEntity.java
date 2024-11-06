package it.pagopa.selfcare.mscore.connector.dao.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Sharded;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@NoArgsConstructor
@Document("PecNotification")
@Sharded(shardKey = {"id"})
@FieldNameConstants(asEnum = true)
public class PecNotificationEntity {

    @BsonId
    private ObjectId id;
    @NotNull
    private String institutionId;
    @NotNull
    private String productId;
    @NotNull
    private Integer moduleDayOfTheEpoch;
    @NotNull
    private String digitalAddress;

    private Instant createdAt;
    private Instant updatedAt;

}