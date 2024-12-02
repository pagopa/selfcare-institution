package it.pagopa.selfcare.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BadGatewayException extends RuntimeException {

    public BadGatewayException(String message) {
        super(message);
    }
}
