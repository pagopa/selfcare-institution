package it.pagopa.selfcare.mscore.web.exception;

import it.pagopa.selfcare.commons.web.model.Problem;
import it.pagopa.selfcare.mscore.constant.GenericError;
import it.pagopa.selfcare.mscore.exception.InvalidRequestException;
import it.pagopa.selfcare.mscore.exception.MsCoreException;
import it.pagopa.selfcare.mscore.exception.ResourceConflictException;
import it.pagopa.selfcare.mscore.exception.ResourceNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
@Slf4j
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(@NonNull MissingServletRequestParameterException ex, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        log.error("InvalidRequestException Occurred --> MESSAGE:{}, STATUS: {}",ex.getMessage(), HttpStatus.BAD_REQUEST, ex);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Problem problem = new Problem(HttpStatus.BAD_REQUEST, "MISSING PARAMETER");
        return new ResponseEntity<>(problem, headers, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex, HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        log.error("InvalidRequestException Occurred --> MESSAGE:{}, STATUS: {}",ex.getMessage(), HttpStatus.BAD_REQUEST, ex);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Problem problem = new Problem(HttpStatus.BAD_REQUEST, "INVALID ARGUMENT");
        return new ResponseEntity<>(problem, headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Problem> handleResourceNotFoundException(HttpServletRequest request, ResourceNotFoundException ex) {
        log.error("ResourceNotFoundException Occurred --> URL:{}, MESSAGE:{}, STATUS: {}",  Encode.forJava(String.valueOf(request.getRequestURL())), ex.getMessage(), HttpStatus.NOT_FOUND, ex);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Problem problem = new Problem( HttpStatus.NOT_FOUND, ex.getMessage());
        return new ResponseEntity<>(problem, headers, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<Problem> handleResourceConflictException(HttpServletRequest request, ResourceConflictException ex) {
        log.error("ResourceConflictException Occurred --> URL:{}, MESSAGE:{}, STATUS: {}",  Encode.forJava(String.valueOf(request.getRequestURL())), ex.getMessage(), HttpStatus.CONFLICT, ex);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Problem problem = new Problem(HttpStatus.CONFLICT, ex.getMessage());
        return new ResponseEntity<>(problem, headers, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Problem> handleInvalidRequestException(HttpServletRequest request, InvalidRequestException ex) {
        log.error("InvalidRequestException Occurred --> URL:{}, MESSAGE:{}, STATUS:{}", Encode.forJava(String.valueOf(request.getRequestURL())), ex.getMessage(), HttpStatus.BAD_REQUEST, ex);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Problem problem = new Problem(HttpStatus.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(problem, headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Problem> handleValidationException(HttpServletRequest request, ValidationException ex) {
        log.error("ValidationException Occurred --> URL:{}, MESSAGE:{}, STATUS:{}", Encode.forJava(String.valueOf(request.getRequestURL())), ex.getMessage(), HttpStatus.BAD_REQUEST, ex);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Problem problem = new Problem(HttpStatus.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(problem, headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MsCoreException.class)
    public ResponseEntity<Problem> handleMsCoreException(HttpServletRequest request, MsCoreException ex) {
        log.error("Exception Occurred --> URL:{}, MESSAGE:{}, STATUS:{}", Encode.forJava(String.valueOf(request.getRequestURL())), ex.getMessage(), INTERNAL_SERVER_ERROR, ex);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Problem problem = new Problem(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return new ResponseEntity<>(problem, headers, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleException(HttpServletRequest request, Exception ex) {
        log.error("{} Occurred --> URL:{}, MESSAGE:{}, STATUS:{}",ex.getCause(), Encode.forJava(String.valueOf(request.getRequestURL())), ex.getMessage(), HttpStatus.BAD_REQUEST, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Problem(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
    }

    private GenericError retrieveGenericError(HttpServletRequest request){
        GenericError genericError = (GenericError) request.getAttribute("errorEnum");
        if(genericError == null){
            genericError = GenericError.GENERIC_ERROR;
        }
        return genericError;
    }
}
