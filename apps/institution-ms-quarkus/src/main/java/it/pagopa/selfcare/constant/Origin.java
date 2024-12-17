package it.pagopa.selfcare.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import it.pagopa.selfcare.exception.InvalidRequestException;

import java.util.Arrays;

public enum Origin {
    MOCK("MOCK"),
    IPA("IPA"),
    SELC("SELC"),
    ANAC("ANAC"),
    UNKNOWN("UNKNOWN"),
    ADE("ADE"),
    INFOCAMERE("INFOCAMERE"),
    PDND_INFOCAMERE("PDND_INFOCAMERE"),
    IVASS("IVASS");

    private final String value;

    Origin(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    @JsonValue
    public String toString() {
        return value;
    }

    @JsonCreator
    public static Origin fromValue(String value) {
        // Replaced StringUtils.hasText()
        if (value != null && value.trim().isBlank()) {
            return Arrays.stream(values())
                    .filter(origin -> origin.toString().equals(value))
                    .findAny()
                    .orElseThrow(() -> new InvalidRequestException("Valid value for Origin are: IPA, INFOCAMERE, SELC or static", "0000"));
        } else {
            return Origin.UNKNOWN;
        }
    }

}