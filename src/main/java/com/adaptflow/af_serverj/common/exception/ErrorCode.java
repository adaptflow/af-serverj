package com.adaptflow.af_serverj.common.exception;

public enum ErrorCode {
    INVALID_INPUT(400, "The input provided is invalid."),
    USER_NOT_FOUND(404, "User not found in the system."),
    SERVER_ERROR(500, "An internal server error occurred."),
    UNAUTHORIZED_ACCESS(401, "Access is unauthorized."),
    BAD_REQUEST(400, "The request is malformed.");

    private int httpStatusCode;
    private String errorMessage;

    // Constructor to initialize HTTP status and error message
    ErrorCode(int httpStatusCode, String errorMessage) {
        this.httpStatusCode = httpStatusCode;
        this.errorMessage = errorMessage;
    }

    // Getter to retrieve the HTTP status code
    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    // Getter to retrieve the error message
    public String getErrorMessage() {
        return errorMessage;
    }
}

