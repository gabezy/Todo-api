package br.com.gabezy.todoapi.config.expectionhandler;

import br.com.gabezy.todoapi.domain.enumaration.ErrorCode;
import br.com.gabezy.todoapi.exceptions.ResourceNotFoundException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Collections;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest webRequest) {
        var error = new ResponseError(ErrorCode.TASK_NOT_FOUND.name(), ex.getMessage(), Collections.emptyMap());
        return this.handleExceptionInternal(ex, error, new HttpHeaders(), HttpStatus.NOT_FOUND, webRequest);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

}
