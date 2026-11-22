package com.rrdiagnocare.exception;

/** A rule violation that should be shown to the user as a friendly message. */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }
}
