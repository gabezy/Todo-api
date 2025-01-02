package br.com.gabezy.todoapi.exceptions;

import br.com.gabezy.todoapi.domain.enumaration.ErrorCode;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage());
    }

}
