package com.ecc.job.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.springframework.hateoas.RepresentationModel;

/**
 * Standard envelope wrapping every API response — either the requested {@code data} on success, or one or more {@link ApiError}s on failure.
 *
 * @param <T> the type of the wrapped payload
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> extends RepresentationModel<ApiResponse<T>> {

    private final ApiStatus status;
    private final T data;
    private final List<ApiError> errors;

    /**
     * Creates a new response envelope. Always called through {@link #success(Object)} or one of the {@code failure} factory methods, never directly.
     *
     * @param status whether this response is a success or a failure
     * @param data the payload on success, or {@code null} on failure
     * @param errors the errors on failure, or an empty list on success
     */
    private ApiResponse(ApiStatus status, T data, List<ApiError> errors) {
        this.status = status;
        this.data = data;
        this.errors = errors;
    }

    /**
     * Builds a successful response wrapping the given data.
     *
     * @param data the payload to return
     * @return an {@code ApiResponse} with status {@link ApiStatus#OK} and no errors
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ApiStatus.OK, data, List.of());
    }

    /**
     * Builds a failure response with a single, non-field-specific error.
     *
     * @param code short, machine-readable error type
     * @param message human-readable description of what went wrong
     * @return an {@code ApiResponse} with status {@link ApiStatus#ERROR} and one error
     */
    public static <T> ApiResponse<T> failure(String code, String message) {
        return failure(code, message, null);
    }

    /**
     * Builds a failure response with a single error tied to a specific request field.
     *
     * @param code short, machine-readable error type
     * @param message human-readable description of what went wrong
     * @param field the request field this error relates to, or {@code null} if it isn't field-specific
     * @return an {@code ApiResponse} with status {@link ApiStatus#ERROR} and one error
     */
    public static <T> ApiResponse<T> failure(String code, String message, String field) {
        return new ApiResponse<>(ApiStatus.ERROR, null, List.of(new ApiError(code, message, field)));
    }

    /**
     * Builds a failure response with multiple errors (e.g. several validation failures at once).
     *
     * @param errors the errors to report
     * @return an {@code ApiResponse} with status {@link ApiStatus#ERROR} and the given errors
     */
    public static <T> ApiResponse<T> failure(List<ApiError> errors) {
        return new ApiResponse<>(ApiStatus.ERROR, null, errors);
    }

    /**
     * Returns whether this response is a success or a failure.
     *
     * @return {@link ApiStatus#OK} on success, or {@link ApiStatus#ERROR} on failure
     */
    public ApiStatus getStatus() {
        return status;
    }

    /**
     * Returns the response payload.
     *
     * @return the payload on success, or {@code null} on failure
     */
    public T getData() {
        return data;
    }

    /**
     * Returns the errors that caused this response to fail.
     *
     * @return the errors on failure, or an empty list on success
     */
    public List<ApiError> getErrors() {
        return errors;
    }
}
