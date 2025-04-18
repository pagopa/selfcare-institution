package it.pagopa.selfcare.delegation.event.entity.mapper;

import it.pagopa.selfcare.delegation.event.entity.DelegationsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "jakarta", uses = {DelegationsEntity.class}, imports = {UUID.class})
public interface DelegationMapper {

        @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
        @Mapping(target = "from", source = "delegationAggregate.from")
        @Mapping(target = "institutionFromName", source = "delegationAggregate.institutionFromName")
        @Mapping(target = "institutionToName", source = "delegationPT.institutionToName")
        @Mapping(target = "institutionFromRootName", source = "delegationAggregate.institutionFromRootName")
        @Mapping(target = "to", source = "delegationPT.to")
        @Mapping(target = "toTaxCode", source = "delegationPT.toTaxCode")
        @Mapping(target = "fromTaxCode", source = "delegationAggregate.fromTaxCode")
        @Mapping(target = "toType", source = "delegationPT.toType")
        @Mapping(target = "fromType", source = "delegationAggregate.fromType")
        @Mapping(target = "productId", source = "delegationPT.productId")
        @Mapping(target = "type", source = "delegationPT.type")
        @Mapping(target = "status", source = "delegationPT.status")
        @Mapping(target = "createdAt", source = "delegationPT.createdAt")
        @Mapping(target = "updatedAt", source = "delegationPT.updatedAt")
        DelegationsEntity toDelegationAggregatePT(DelegationsEntity delegationAggregate, DelegationsEntity delegationPT);

}
