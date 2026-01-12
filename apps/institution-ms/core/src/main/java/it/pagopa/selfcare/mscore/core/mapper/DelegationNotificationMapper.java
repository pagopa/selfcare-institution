package it.pagopa.selfcare.mscore.core.mapper;

import it.pagopa.selfcare.mscore.constant.EventType;
import it.pagopa.selfcare.mscore.model.DelegationNotificationToSend;
import it.pagopa.selfcare.mscore.model.delegation.Delegation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.OffsetDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = UUID.class)
public interface DelegationNotificationMapper {

    @Mapping(target = "eventType", expression = "java(toEventType(delegation.getUpdatedAt(), delegation.getClosedAt()))")
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "delegationId", source = "id")
    @Mapping(target = "fromType", source = "institutionType")
    @Mapping(target = "toType", source = "brokerType")
    DelegationNotificationToSend toDelegationNotificationToSend(Delegation delegation);

    default String toOffsetDateTimeString(OffsetDateTime dateTime) {
        return dateTime != null ? dateTime.toString() : null;
    }

    default EventType toEventType(OffsetDateTime updatedAt, OffsetDateTime closedAt) {
        return updatedAt != null || closedAt != null ? EventType.UPDATE : EventType.ADD;
    }

}
