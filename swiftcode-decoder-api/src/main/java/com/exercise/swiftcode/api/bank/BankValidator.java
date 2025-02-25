package com.exercise.swiftcode.api.bank;

import com.exercise.swiftcode.api.bank.request.CreateBankRequest;
import com.exercise.swiftcode.config.exceptions.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class BankValidator {
    private static final int SWIFT_CODE_LENGTH = 11;
    private static final int COUNTRY_ISO2_LENGTH = 2;
    private static final String HEADQUARTER_SUFFIX_REGEX = "(?i).*xxx$";

    public void validateCreateBankRequest(CreateBankRequest request) {
        if (request == null) {
            throw new ValidationException("CreateBankRequest cannot be null.");
        }
        validateSwiftCode(request.getSwiftCode());
        validateHeadquarterSwiftCode(request.getSwiftCode(), request.getIsHeadquarter());
        validateBankName(request.getBankName());
        validateAddress(request.getAddress());
        validateCountryIso2Length(request.getCountryISO2());
        validateCountryName(request.getCountryName());
    }

    public void validateDeleteBankRequest(String swiftCode) {
        validateSwiftCode(swiftCode);
    }

    public void validateSwiftCode(String swiftCode) {
        if (swiftCode == null) {
            throw new ValidationException("SWIFT Code cannot be null.");
        }
        validateSwiftCodeLength(swiftCode);
        validateSwiftCodeCharacters(swiftCode);
    }

    private void validateSwiftCodeLength(String swiftCode) {
        if (swiftCode.length() != SWIFT_CODE_LENGTH) {
            throw new ValidationException("SWIFT Code must be exactly " + SWIFT_CODE_LENGTH + " characters long.");
        }
    }

    private void validateSwiftCodeCharacters(String swiftCode) {
        if (!swiftCode.matches("^[A-Za-z0-9]+$")) {
            throw new ValidationException("SWIFT Code must contain only uppercase letters and numbers.");
        }
    }

    private void validateHeadquarterSwiftCode(String swiftCode, boolean isHeadquarter) {
        if (isHeadquarter && !swiftCode.matches(HEADQUARTER_SUFFIX_REGEX)) {
            throw new ValidationException("Headquarter SWIFT Codes must end with XXX.");
        }
    }

    public void validateCountryIso2Length(String iso2) {
        if (iso2 == null) {
            throw new ValidationException("Country ISO2 code cannot be null.");
        }
        if (iso2.length() != COUNTRY_ISO2_LENGTH) {
            throw new ValidationException("Country ISO2 code must be exactly " + COUNTRY_ISO2_LENGTH + " characters long.");
        }
    }

    private void validateBankName(String bankName) {
        if (bankName == null || bankName.trim().isEmpty()) {
            throw new ValidationException("Bank name cannot be null or empty.");
        }
    }

    private void validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new ValidationException("Address cannot be null or empty.");
        }
    }

    private void validateCountryName(String countryName) {
        if (countryName == null || countryName.trim().isEmpty()) {
            throw new ValidationException("Country name cannot be null or empty.");
        }
    }
}
