package com.ecc.job.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A single error included in an {@link ApiResponse} when a request fails.
 *
 * @param code short, machine-readable error type (e.g. {@code "NOT_FOUND"}, {@code "INVALID_STATE"}, {@code "VALIDATION_ERROR"})
 * @param message human-readable description of what went wrong
 * @param field the request field this error relates to, or {@code null} if it isn't field-specific
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String code, String message, String field) {}
