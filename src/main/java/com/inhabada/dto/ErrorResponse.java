package com.inhabada.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String error,
        String message,
        List<String> fields
) {
    public ErrorResponse(String error, String message) {
        this(error, message, null);
    }
}
