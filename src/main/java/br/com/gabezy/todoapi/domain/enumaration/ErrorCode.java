package br.com.gabezy.todoapi.domain.enumaration;

public enum ErrorCode {

    TASK_NOT_FOUND("Task not found"),
    INVALID_FIELDS("The request body has invalid fields");

    private String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
