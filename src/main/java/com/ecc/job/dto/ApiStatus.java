package com.ecc.job.dto;

/**
 * Overall outcome of an {@link ApiResponse}.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public enum ApiStatus {
    /** The request succeeded; {@link ApiResponse#getData()} carries the result. */
    OK,
    /** The request failed; {@link ApiResponse#getErrors()} carries the reason. */
    ERROR,
}
