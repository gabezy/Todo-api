package br.com.gabezy.todoapi.exceptions;

import br.com.gabezy.todoapi.domain.enumaration.ErrorCode;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(ErrorCode errorCode) {
        super(errorCode.getMessage());
    }

}
