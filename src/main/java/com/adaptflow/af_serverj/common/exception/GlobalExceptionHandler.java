package com.adaptflow.af_serverj.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handle the CustomException
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(ServiceException ex) {
        // Prepare the error response
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage()
        );
        ex.printStackTrace();

        // Return the error response with the appropriate HTTP status code
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.getHttpStatusCode()));
    }

    // Optionally handle other exceptions globally
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                ex.getMessage()
        );
        ex.printStackTrace();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
