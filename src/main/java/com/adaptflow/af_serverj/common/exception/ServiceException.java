package com.adaptflow.af_serverj.common.exception;

public class ServiceException extends Exception {
    
    /**
	 * Custom exception class of adaptflow
	 */
	private static final long serialVersionUID = 1L;
    private int httpStatusCode;
    private ErrorCode errorCode;  // Use the enum for errorCode
    private String errorMessage;
    
    // Constructor
    public ServiceException(ErrorCode errorCode) {
        super(errorCode.getErrorMessage());  // Pass the error message from the enum to the Exception class constructor
        this.httpStatusCode = errorCode.getHttpStatusCode();
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getErrorMessage();
    }

    // Getter for HTTP Status Code
    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    // Getter for Error Code
    public String getErrorCode() {
        return errorCode.name();  // Return the name of the enum constant
    }

    // Getter for Error Message
    @Override
    public String getMessage() {
        return errorMessage;
    }

    // Optional: Overriding toString() to provide detailed exception information
    @Override
    public String toString() {
        return "HTTP Status: " + httpStatusCode + ", Error Code: " + errorCode.name() + ", Message: " + errorMessage;
    }
}

