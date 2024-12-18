package it.pagopa.selfcare.model.mapper;

import it.pagopa.selfcare.model.institution.*;
import it.pagopa.selfcare.onboarding.common.InstitutionType;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InstitutionMapperCustom {

    protected static final BinaryOperator<BulkProduct> MERGE_FUNCTION = (inst1, inst2) -> inst1.getStatus().compareTo(inst2.getStatus()) < 0 ? inst1 : inst2;

    public static BulkInstitutions toBulkInstitutions(List<Institution> institution, List<String> idsRequest) {
        BulkInstitutions bulkInstitutions = new BulkInstitutions();
        bulkInstitutions.setFound(institution.stream()
                .map(InstitutionMapperCustom::toBulkInstitution)
                .toList());
        bulkInstitutions.setNotFound(idsRequest.stream()
                .filter(s -> institution.stream().noneMatch(inst -> inst.getId().equalsIgnoreCase(s)))
                .toList());
        return bulkInstitutions;
    }

    private static BulkInstitution toBulkInstitution(Institution inst) {
        BulkInstitution bulkInstitution = new BulkInstitution();
        bulkInstitution.setId(inst.getId());
        bulkInstitution.setExternalId(inst.getExternalId());
        bulkInstitution.setOrigin(inst.getOrigin());
        bulkInstitution.setOriginId(inst.getOriginId());
        bulkInstitution.setDescription(inst.getDescription());
        bulkInstitution.setInstitutionType(Optional.ofNullable(inst.getInstitutionType()).map(InstitutionType::name).orElse(null));
        bulkInstitution.setDigitalAddress(inst.getDigitalAddress());
        bulkInstitution.setAddress(inst.getAddress());
        bulkInstitution.setZipCode(inst.getZipCode());
        bulkInstitution.setTaxCode(inst.getTaxCode());
        bulkInstitution.setAttributes(toAttributeResponse(inst.getAttributes()));
        bulkInstitution.setProducts(toBulkProductMap(inst.getOnboarding(), inst));
        return bulkInstitution;
    }

    public static List<AttributesResponse> toAttributeResponse(List<Attributes> attributes) {
        List<AttributesResponse> list = new ArrayList<>();
        if (attributes != null && !attributes.isEmpty()) {
            for (Attributes a : attributes) {
                AttributesResponse response = new AttributesResponse();
                response.setCode(a.getCode());
                response.setOrigin(a.getOrigin());
                response.setDescription(a.getDescription());
                list.add(response);
            }
        }
        return list;
    }

    private static Map<String, BulkProduct> toBulkProductMap(List<Onboarding> onboarding, Institution institution) {
        if (onboarding != null && !onboarding.isEmpty()) {
            return onboarding.stream().map(onb -> {
                BulkProduct bulkProduct = new BulkProduct();
                bulkProduct.setProduct(onb.getProductId());
                bulkProduct.setPricingPlan(onb.getPricingPlan());
                bulkProduct.setBilling(toBillingResponse(onb.getBilling(), institution));
                bulkProduct.setStatus(onb.getStatus());
                return bulkProduct;
            }).collect(Collectors.toMap(BulkProduct::getProduct, Function.identity(), MERGE_FUNCTION));
        }
        return Collections.emptyMap();
    }

    private static BillingResponse toBillingResponse(Billing billing, Institution institution) {
        BillingResponse billingResponse = new BillingResponse();
        if (billing != null) {
            billingResponse.setVatNumber(billing.getVatNumber());
            billingResponse.setTaxCodeInvoicing(billing.getTaxCodeInvoicing());
            billingResponse.setRecipientCode(billing.getRecipientCode());
            billingResponse.setPublicServices(billing.isPublicServices());
        } else if (institution.getBilling() != null) {
            billingResponse.setVatNumber(institution.getBilling().getVatNumber());
            billingResponse.setTaxCodeInvoicing(institution.getBilling().getTaxCodeInvoicing());
            billingResponse.setRecipientCode(institution.getBilling().getRecipientCode());
            billingResponse.setPublicServices(institution.getBilling().isPublicServices());
        }
        return billingResponse;
    }

}
