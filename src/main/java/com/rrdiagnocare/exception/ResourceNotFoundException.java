package com.rrdiagnocare.exception;

/** Thrown when a requested booking / test / package does not exist or is not visible to the user. */
public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
