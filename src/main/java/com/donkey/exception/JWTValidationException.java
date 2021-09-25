package com.donkey.exception;

public class JWTValidationException extends RuntimeException{

    public JWTValidationException() {
        super();
    }

    public JWTValidationException(String message) {
        super(message);
    }

    public JWTValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JWTValidationException(Throwable cause) {
        super(cause);
    }
}
