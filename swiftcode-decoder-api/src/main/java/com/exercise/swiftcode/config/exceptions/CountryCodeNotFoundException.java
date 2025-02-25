package com.exercise.swiftcode.config.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CountryCodeNotFoundException extends RuntimeException {
    public CountryCodeNotFoundException(String message) {
        super(message);
    }
}
