package com.ecc.job.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@JsonInclude( JsonInclude.Include.NON_NULL )
public class ApiResponse<T> extends RepresentationModel<ApiResponse<T>>
{
    private final String status;
    private final T data;
    private final List<ApiError> errors;

    private ApiResponse( String aStatus, T aData, List<ApiError> aErrors )
    {
        this.status = aStatus;
        this.data = aData;
        this.errors = aErrors;
    }

    public static <T> ApiResponse<T> success( T data )
    {
        return new ApiResponse<>( "OK", data, List.of() );
    }

    public static <T> ApiResponse<T> failure( String code, String message )
    {
        return new ApiResponse<>( "ERROR", null, List.of( new ApiError( code, message ) ) );
    }

    public static <T> ApiResponse<T> failure( List<ApiError> errors )
    {
        return new ApiResponse<>( "ERROR", null, errors );
    }

    public String getStatus() { return status; }
    public T getData() { return data; }
    public List<ApiError> getErrors() { return errors; }
}
