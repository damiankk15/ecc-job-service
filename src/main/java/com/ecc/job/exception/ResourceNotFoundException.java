package com.ecc.job.exception;

/**
 * Thrown when a request refers to a resource (e.g. a job) that doesn't exist. Mapped by {@link GlobalExceptionHandler} to {@code 404 Not Found}.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates a new exception with the given message.
     *
     * @param message describes which resource was not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
