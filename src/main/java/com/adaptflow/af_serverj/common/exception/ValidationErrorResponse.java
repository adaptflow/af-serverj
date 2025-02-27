package com.adaptflow.af_serverj.common.exception;

import java.util.Map;

public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> validationErrors;

    public ValidationErrorResponse(String errorCode, String errorMessage, Map<String, String> validationErrors) {
        super(errorCode, errorMessage);
        this.validationErrors = validationErrors;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}