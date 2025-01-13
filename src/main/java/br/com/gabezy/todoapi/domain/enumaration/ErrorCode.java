package br.com.gabezy.todoapi.domain.enumaration;

import java.util.Arrays;

public enum ErrorCode {

    TASK_NOT_FOUND("Task not found"),
    USER_NOT_FOUND("User not found"),
    ROLE_NOT_FOUND("Role not found"),
    MISSING_TOKEN("Authorization Token is missing"),
    INVALID_FIELDS("The request body has invalid fields"),
    USER_NOT_AUTHENTICATED("User not authenticated"),
    USER_NOT_AUTHORIZED("User not authorized");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static ErrorCode getErrorCodeByMessage(String message) {
        return Arrays.stream(ErrorCode.values())
                .filter(errorCode -> errorCode.message.equals(message))
                .findFirst()
                .orElseThrow(RuntimeException::new);
    }
}
