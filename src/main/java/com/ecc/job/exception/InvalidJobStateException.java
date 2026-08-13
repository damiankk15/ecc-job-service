package com.ecc.job.exception;

/**
 * Thrown when a request would move a job into a state its current status doesn't allow (e.g. cancelling a job that isn't running or queued). Mapped
 * by {@link GlobalExceptionHandler} to {@code 400 Bad Request}.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public class InvalidJobStateException extends RuntimeException {

    /**
     * Creates a new exception with the given message.
     *
     * @param message describes why the current state doesn't allow the requested transition
     */
    public InvalidJobStateException(String message) {
        super(message);
    }
}
