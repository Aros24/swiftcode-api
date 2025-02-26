package com.exercise.swiftcode.api.bank;

import com.exercise.swiftcode.api.bank.request.CreateBankRequest;
import com.exercise.swiftcode.config.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class BankValidatorTest {
    private BankValidator bankValidator;
    private CreateBankRequest validRequest;

    private static final String BANK_NAME = "Bank Name";
    private static final String BANK_ADDRESS = "Bank Address";

    private static final String BANK_COUNTRY_NAME = "Country Name";
    private static final String BANK_COUNTRY_CODE = "CN";
    private static final String BANK_COUNTRY_CODE_INVALID_LENGTH= "CNA";

    private static final String BANK_SWIFTCODE_HQ ="TESTUS33XXX";
    private static final String BANK_SWIFTCODE_BRANCH = "TESTUS33ABC";
    private static final String BANK_INVALID_CHARACTERS_SWIFTCODE = "TES#US33XXX";
    private static final String BANK_TOO_SMALL_SWIFTCODE = "TUS33XXX";


    @BeforeEach
    void setUp() {
        bankValidator = new BankValidator();
        validRequest = CreateBankRequest.builder()
                .swiftCode(BANK_SWIFTCODE_HQ)
                .isHeadquarter(true)
                .bankName(BANK_NAME)
                .address(BANK_ADDRESS)
                .countryISO2(BANK_COUNTRY_CODE)
                .countryName(BANK_COUNTRY_NAME)
                .build();
    }

    @Test
    void validateSwiftCode_whenSwiftCodeIsNull_throwsValidationException() {
        // Given
        String swiftCode = null;

        // When & Then
        assertThatThrownBy(() -> bankValidator.validateSwiftCode(swiftCode))
                .isInstanceOf(ValidationException.class)
                .hasMessage("SWIFT Code cannot be null.");
    }

    @Test
    void validateSwiftCode_whenSwiftCodeLengthIsInvalid_throwsValidationException() {
        // When & Then
        assertThatThrownBy(() -> bankValidator.validateSwiftCode(BANK_TOO_SMALL_SWIFTCODE))
                .isInstanceOf(ValidationException.class)
                .hasMessage("SWIFT Code must be exactly 11 characters long.");
    }

    @Test
    void validateSwiftCode_whenSwiftCodeHasInvalidCharacters_throwsValidationException() {
        // When & Then
        assertThatThrownBy(() -> bankValidator.validateSwiftCode(BANK_INVALID_CHARACTERS_SWIFTCODE))
                .isInstanceOf(ValidationException.class)
                .hasMessage("SWIFT Code must contain only uppercase letters and numbers.");
    }

    @Test
    void validateDeleteBankRequest_whenSwiftCodeIsValid_doesNotThrowException() {
        // When
        bankValidator.validateDeleteBankRequest(BANK_SWIFTCODE_HQ);

        // Then (no exception thrown)
    }

    @Test
    void validateHeadquarterSwiftCode_whenIsHeadquarterTrueAndNoXXX_throwsValidationException() {
        // Given
        CreateBankRequest invalidRequest = validRequest.toBuilder()
                .isHeadquarter(true)
                .swiftCode(BANK_SWIFTCODE_BRANCH)
                .build();

        // When & Then
        assertThatThrownBy(() -> bankValidator.validateCreateBankRequest(invalidRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Headquarter SWIFT Codes must end with XXX.");
    }

    @Test
    void validateCreateBankRequest_whenBankNameIsNull_throwsValidationException() {
        // Given
        CreateBankRequest request = validRequest.toBuilder()
                .bankName(null)
                .build();

        // When & Then
        assertThatThrownBy(() -> bankValidator.validateCreateBankRequest(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Bank name cannot be null or empty.");
    }

    @Test
    void validateCreateBankRequest_whenBankNameIsEmpty_throwsValidationException() {
        // Given
        CreateBankRequest request = validRequest.toBuilder()
                .bankName("")
                .build();

        // When & Then
        assertThatThrownBy(() -> bankValidator.validateCreateBankRequest(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Bank name cannot be null or empty.");
    }

    @Test
    void validateCreateBankRequest_whenAddressIsNull_throwsValidationException() {
        // Given
        CreateBankRequest request = validRequest.toBuilder()
                .address(null)
                .build();

        // When & Then
        assertThatThrownBy(() -> bankValidator.validateCreateBankRequest(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Address cannot be null or empty.");
    }

    @Test
    void validateCreateBankRequest_whenAddressIsBlank_throwsValidationException() {
        // Given
        CreateBankRequest request = validRequest.toBuilder()
                .address("   ")
                .build();

        // When & Then
        assertThatThrownBy(() -> bankValidator.validateCreateBankRequest(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Address cannot be null or empty.");
    }

    @Test
    void validateCountryIso2Length_whenCountryIso2IsNull_throwsValidationException() {
        // Given
        String countryIso2 = null;

        // When & Then
        assertThatThrownBy(() -> bankValidator.validateCountryIso2Length(countryIso2))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Country ISO2 code cannot be null.");
    }

    @Test
    void validateCountryIso2Length_whenCountryIso2LengthIsInvalid_throwsValidationException() {
        // When & Then
        assertThatThrownBy(() -> bankValidator.validateCountryIso2Length(BANK_COUNTRY_CODE_INVALID_LENGTH))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Country ISO2 code must be exactly 2 characters long.");
    }

    @Test
    void validateCreateBankRequest_whenCountryNameIsNull_throwsValidationException() {
        // Given
        CreateBankRequest request = validRequest.toBuilder()
                .countryName(null)
                .build();

        // When & Then
        assertThatThrownBy(() -> bankValidator.validateCreateBankRequest(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Country name cannot be null or empty.");
    }

    @Test
    void validateCreateBankRequest_whenCountryNameIsEmpty_throwsValidationException() {
        // Given
        CreateBankRequest request = validRequest.toBuilder()
                .countryName("")
                .build();

        // When & Then
        assertThatThrownBy(() -> bankValidator.validateCreateBankRequest(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Country name cannot be null or empty.");
    }

    @Test
    void validateCreateBankRequest_whenRequestIsNull_throwsValidationException() {
        // Given
        CreateBankRequest request = null;

        // When & Then
        assertThatThrownBy(() -> bankValidator.validateCreateBankRequest(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("CreateBankRequest cannot be null.");
    }

    @Test
    void validateCreateBankRequest_whenRequestIsValid_doesNotThrowException() {
        // Given
        CreateBankRequest request = validRequest;

        // When
        bankValidator.validateCreateBankRequest(request);

        // Then (no exception thrown)
    }
}
