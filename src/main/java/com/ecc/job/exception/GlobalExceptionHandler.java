package com.ecc.job.exception;

import com.ecc.job.dto.ApiError;
import com.ecc.job.dto.ApiResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Translates exceptions thrown anywhere in the request-handling chain into the API's standard {@link ApiResponse} error envelope, so every failure —
 * expected or not — comes back in the same shape.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles a missing resource.
     *
     * @param ex the exception
     * @return {@code 404 Not Found} with code {@code "NOT_FOUND"}
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleResourceNotFound(ResourceNotFoundException ex) {
        return ApiResponse.failure("NOT_FOUND", ex.getMessage());
    }

    /**
     * Handles a query/path parameter that couldn't be converted to its declared type (e.g. an unparseable ISO-8601 instant).
     *
     * @param ex the exception
     * @return {@code 400 Bad Request} with code {@code "INVALID_REQUEST"}
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ApiResponse.failure("INVALID_REQUEST", "Invalid value for '" + ex.getName() + "': " + ex.getValue());
    }

    /**
     * Handles a request body that couldn't be read (e.g. malformed JSON, or a value of the wrong JSON type for its field).
     *
     * @param ex the exception
     * @return {@code 400 Bad Request} with code {@code "INVALID_REQUEST"}
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return ApiResponse.failure("INVALID_REQUEST", "Malformed request body");
    }

    /**
     * Handles a request that would move a job into a state its current status doesn't allow.
     *
     * @param ex the exception
     * @return {@code 400 Bad Request} with code {@code "INVALID_STATE"}
     */
    @ExceptionHandler(InvalidJobStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleInvalidJobState(InvalidJobStateException ex) {
        return ApiResponse.failure("INVALID_STATE", ex.getMessage());
    }

    /**
     * Handles Bean Validation failures on a {@code @Valid} request body, reporting one {@link ApiError} per invalid field.
     *
     * @param ex the exception
     * @return {@code 400 Bad Request} with one {@code "VALIDATION_ERROR"} per invalid field
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiError> errors = ex
            .getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ApiError("VALIDATION_ERROR", message(error), error.getField()))
            .toList();

        return ApiResponse.failure(errors);
    }

    /**
     * Catch-all for any exception not handled more specifically above.
     *
     * @param ex the exception
     * @return {@code 500 Internal Server Error} with code {@code "INTERNAL_ERROR"}
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneric(Exception ex) {
        return ApiResponse.failure("INTERNAL_ERROR", ex.getMessage());
    }

    /**
     * Returns a field error's default message, falling back to a generic one if it has none.
     *
     * @param error the field error
     * @return the error's default message, or {@code "Invalid value"} if it has none
     */
    private static String message(FieldError error) {
        return error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value";
    }
}
