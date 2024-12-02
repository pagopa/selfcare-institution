package it.pagopa.selfcare.exception;

public class MsCoreException extends RuntimeException {

    private final String code;

    public MsCoreException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
