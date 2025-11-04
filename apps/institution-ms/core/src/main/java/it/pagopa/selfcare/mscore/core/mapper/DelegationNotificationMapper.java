package it.pagopa.selfcare.mscore.core.mapper;

import it.pagopa.selfcare.mscore.constant.EventType;
import it.pagopa.selfcare.mscore.model.DelegationNotificationToSend;
import it.pagopa.selfcare.mscore.model.delegation.Delegation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface DelegationNotificationMapper {

    @Mapping(target = "eventType", expression = "java(toEventType(delegation.getUpdatedAt(), delegation.getClosedAt()))")
    DelegationNotificationToSend toDelegationNotificationToSend(Delegation delegation);

    default String toOffsetDateTimeString(OffsetDateTime dateTime) {
        return dateTime != null ? dateTime.toString() : null;
    }

    default EventType toEventType(OffsetDateTime updatedAt, OffsetDateTime closedAt) {
        return updatedAt != null || closedAt != null ? EventType.UPDATE : EventType.ADD;
    }

}
