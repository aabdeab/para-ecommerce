package com.ecommerce.DTOs;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp,
        Map<String,Object> metadata
) {
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }
    public static <T> ApiResponse<T> success(T data, String message){
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .message(message)
                .build();
    }
    public static <T> ApiResponse<T> error(String message,Map<String,Object> metadata){
        return ApiResponse.<T>builder()
                .success(false)
                .timestamp(Instant.now())
                .message(message)
                .metadata(metadata)
                .build();
    }
    public   ApiResponse<T> withMetadata(String key,Object value){
        Map<String,Object> newMetadata = this.metadata!=null ? new HashMap<>(this.metadata) : new HashMap<>();
        newMetadata.put(key,value);
        return new ApiResponse<T>(
                this.success,
                this.message,
                this.data,
                this.timestamp,
                newMetadata
        );
    }

}
