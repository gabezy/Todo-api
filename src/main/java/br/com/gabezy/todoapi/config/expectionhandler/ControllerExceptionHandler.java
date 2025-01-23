package br.com.gabezy.todoapi.config.expectionhandler;

import br.com.gabezy.todoapi.domain.enumaration.ErrorCode;
import br.com.gabezy.todoapi.exceptions.InvalidCredentialsException;
import br.com.gabezy.todoapi.exceptions.ResourceNotFoundException;
import org.springframework.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    private static final ErrorCode INVALID_FIELDS = ErrorCode.INVALID_FIELDS;
    private static final ErrorCode INTERNAL_SERVER_ERROR = ErrorCode.INTERNAL_ERROR_SERVER;

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> fields = ex.getAllErrors().stream()
                .filter(FieldError.class::isInstance)
                .map(FieldError.class::cast)
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        var error = new ResponseError(INVALID_FIELDS.name(), INVALID_FIELDS.getMessage(), fields);
        return this.handleExceptionInternal(ex, error, new HttpHeaders(headers), status, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorCode errorCode = ErrorCode.getErrorCodeByMessage(ex.getMessage());
        var error = new ResponseError(errorCode.name(), ex.getMessage(), Collections.emptyMap());
        return this.handleExceptionInternal(ex, error, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Object> handleInvalidCredentialsException(InvalidCredentialsException ex, WebRequest request) {
        ErrorCode errorCode = ErrorCode.getErrorCodeByMessage(ex.getMessage());
        var error = new ResponseError(errorCode.name(), ex.getMessage(), Collections.emptyMap());
        return this.handleExceptionInternal(ex, error, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        return this.handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllException(Exception ex, WebRequest request) {
        var error = new ResponseError(INTERNAL_SERVER_ERROR.name(), INTERNAL_SERVER_ERROR.getMessage(), Collections.emptyMap());
        return this.handleExceptionInternal(ex, error, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

}
