package com.exercise.swiftcode.config.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ErrorResponse {
  @Schema(description = "The error code associated with the issue (e.g., 400, 409, etc.)")
  int statusCode;

  @Schema(description = "The error message describing what went wrong")
  String message;

  @Schema(description = "The timestamp of when the error occurred")
  LocalDateTime timestamp;

  @Schema(description = "Stack trace")
  String[] stackTrace;

  public ErrorResponse(int statusCode, String message, Throwable exception) {
    this.statusCode = statusCode;
    this.message = message;
    this.timestamp = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();
    this.stackTrace = exception != null ? Arrays.stream(exception.getStackTrace())
            .map(StackTraceElement::toString)
            .toArray(String[]::new) : null;
  }
}
