package it.pagopa.selfcare.institution.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Getter;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

@Getter
@MongoEntity(collection="pecNotification")
public class PecNotification {
    @BsonId
    private ObjectId id;
    private Integer moduleDayOfTheEpoch;
    private String productId;
    private String institutionId;

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setModuleDayOfTheEpoch(Integer moduleDayOfTheEpoch) {
        this.moduleDayOfTheEpoch = moduleDayOfTheEpoch;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }

    @Override
    public String toString() {
        return "PecNotification{" +
                "id=" + id +
                ", moduleDayOfTheEpoch=" + moduleDayOfTheEpoch +
                ", productId='" + productId + '\'' +
                ", institutionId='" + institutionId + '\'' +
                '}';
    }
}
