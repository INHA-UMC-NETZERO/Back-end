package com.inhabada.exception;

import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<String> fields;

    public ValidationException(String message) {
        super(message);
        this.fields = null;
    }

    public ValidationException(String message, List<String> fields) {
        super(message);
        this.fields = fields;
    }

    public List<String> getFields() {
        return fields;
    }
}
