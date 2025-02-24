package com.exercise.swiftcode.config.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateSwiftCodeException extends RuntimeException {
    public DuplicateSwiftCodeException(String message) {
        super(message);
    }
}