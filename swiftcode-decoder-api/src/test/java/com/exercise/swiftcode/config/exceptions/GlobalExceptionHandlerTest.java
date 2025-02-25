package com.exercise.swiftcode.config.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.Path;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {
    @InjectMocks private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleBankNotFoundException_returnsNotFoundResponse() {
        // Given
        BankNotFoundException ex = new BankNotFoundException("Bank with SWIFT Code 'TESTCN33XXX' not found");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleBankNotFoundException(ex);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(Objects.requireNonNull(response.getBody()).getStatusCode()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Bank with SWIFT Code 'TESTCN33XXX' not found");
        assertThat(response.getBody().getStackTrace()).isNull();
    }

    @Test
    void handleDuplicateSwiftCodeException_returnsConflictResponse() {
        // Given
        DuplicateSwiftCodeException ex = new DuplicateSwiftCodeException("SWIFT Code already exists");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleDuplicateSwiftCodeException(ex);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(Objects.requireNonNull(response.getBody()).getStatusCode()).isEqualTo(409);
        assertThat(response.getBody().getMessage()).isEqualTo("SWIFT Code already exists");
        assertThat(response.getBody().getStackTrace()).isNull();
    }

    @Test
    void handleValidationException_returnsBadRequestResponse() {
        // Given
        ValidationException ex = new ValidationException("Invalid input");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(Objects.requireNonNull(response.getBody()).getStatusCode()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid input");
        assertThat(response.getBody().getStackTrace()).isNull();
    }

    @Test
    void handleCountryCodeNotFoundException_returnsNotFoundResponse() {
        // Given
        CountryCodeNotFoundException ex = new CountryCodeNotFoundException("Country ISO2 code 'CN' does not exist");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleCountryCodeNotFoundException(ex);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(Objects.requireNonNull(response.getBody()).getStatusCode()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Country ISO2 code 'CN' does not exist");
        assertThat(response.getBody().getStackTrace()).isNull();
    }

    @Test
    void handleHttpMessageNotReadable_returnsBadRequestResponse() {
        // Given
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(ex);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(Objects.requireNonNull(response.getBody()).getStatusCode()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("Invalid request body format Malformed JSON");
        assertThat(response.getBody().getStackTrace()).isNull();
    }

    @Test
    void handleGenericException_logsErrorAndReturnsInternalServerError() {
        // Given
        Exception ex = new Exception("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(Objects.requireNonNull(response.getBody()).getStatusCode()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected internal error occurred.");
        assertThat(response.getBody().getStackTrace()).isNull();
    }

    @Test
    void handleConstraintViolationException_returnsBadRequestError() {
        // Given
        ConstraintViolationException ex = mock(ConstraintViolationException.class);

        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("must not be blank");

        Path path = mock(Path.class);
        Iterator<Path.Node> pathIterator = mock(Iterator.class);
        Path.Node node = mock(Path.Node.class);
        when(node.getName()).thenReturn("bankName");
        when(path.iterator()).thenReturn(pathIterator);
        when(pathIterator.hasNext()).thenReturn(true, false);
        when(pathIterator.next()).thenReturn(node);
        when(violation.getPropertyPath()).thenReturn(path);
        when(ex.getConstraintViolations()).thenReturn(Set.of(violation));

        // When
        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolationException(ex);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(Objects.requireNonNull(response.getBody()).getStatusCode()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("must not be blank");
        assertThat(response.getBody().getStackTrace()).isNull();
    }

    @Test
    void handleMethodArgumentNotValidException_returnsBadRequestError() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new FieldError("object", "swiftCode", "must not be null"));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValidException(ex);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(Objects.requireNonNull(response.getBody()).getStatusCode()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("swiftCode: must not be null");
        assertThat(response.getBody().getStackTrace()).isNull();
    }
}