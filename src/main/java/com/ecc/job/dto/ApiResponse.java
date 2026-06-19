package com.ecc.job.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude( JsonInclude.Include.NON_NULL )
public record ApiResponse<T>( String status, T data, List<ApiError> errors )
{
    public static <T> ApiResponse<T> success( T data )
    {
        return new ApiResponse<>( "SUCCESS", data, null );
    }

    public static <T> ApiResponse<T> failure( String code, String message )
    {
        return new ApiResponse<>( "FAILURE", null, List.of( new ApiError( code, message ) ) );
    }

    public static <T> ApiResponse<T> failure( List<ApiError> errors )
    {
        return new ApiResponse<>( "FAILURE", null, errors );
    }
}
