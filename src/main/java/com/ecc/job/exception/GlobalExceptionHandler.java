package com.ecc.job.exception;

import com.ecc.job.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler( ResourceNotFoundException.class )
    @ResponseStatus( HttpStatus.NOT_FOUND )
    public ApiResponse<?> handleResourceNotFound( ResourceNotFoundException ex )
    {
        return ApiResponse.failure( "NOT_FOUND", ex.getMessage() );
    }

    @ExceptionHandler( Exception.class )
    @ResponseStatus( HttpStatus.INTERNAL_SERVER_ERROR )
    public ApiResponse<?> handleGeneric( Exception ex )
    {
        return ApiResponse.failure( "INTERNAL_ERROR", ex.getMessage() );
    }
}
