package it.pagopa.selfcare.entity.inner;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants(asEnum = true)
public class AttributesEntity {

    private String origin;
    private String code;
    private String description;

}
