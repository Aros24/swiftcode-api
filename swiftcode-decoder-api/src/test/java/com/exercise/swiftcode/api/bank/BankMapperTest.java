package com.exercise.swiftcode.api.bank;

import com.exercise.swiftcode.api.bank.request.CreateBankRequest;
import com.exercise.swiftcode.api.bank.response.BankResponse;
import com.exercise.swiftcode.persistence.entity.Bank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BankMapperTest {
    private BankMapper bankMapper;

    private static final String BANK_NAME = "Bank Name";
    private static final String BANK_ADDRESS = "Bank Address";
    private static final String BANK_COUNTRY_NAME = "Country Name";
    private static final String BANK_COUNTRY_CODE = "CN";
    private static final String BANK_SWIFT_CODE ="TESTUS33XXX";

    @BeforeEach
    void setUp() {
        bankMapper = new BankMapper();
    }

    @Test
    void toBank_whenRequestIsValid_mapsFieldsCorrectly() {
        // Given
        CreateBankRequest request = CreateBankRequest.builder()
                .swiftCode(BANK_SWIFT_CODE)
                .bankName(BANK_NAME)
                .address(BANK_ADDRESS)
                .countryISO2(BANK_COUNTRY_CODE)
                .countryName(BANK_COUNTRY_NAME)
                .isHeadquarter(true)
                .build();

        // When
        Bank bank = bankMapper.toBank(request);

        // Then
        assertThat(bank.getSwiftCode()).isEqualTo(BANK_SWIFT_CODE);
        assertThat(bank.getName()).isEqualTo(BANK_NAME);
        assertThat(bank.getAddress()).isEqualTo(BANK_ADDRESS);
        assertThat(bank.getCountryIso2Code()).isEqualTo(BANK_COUNTRY_CODE);
        assertThat(bank.getCountryName()).isEqualTo(BANK_COUNTRY_NAME);
    }

    @Test
    void toBankResponse_whenBankIsValid_mapsFieldsCorrectly() {
        // Given
        Bank bank = Bank.builder()
                .swiftCode(BANK_SWIFT_CODE)
                .name(BANK_NAME)
                .address(BANK_ADDRESS)
                .countryIso2Code(BANK_COUNTRY_CODE)
                .countryName(BANK_COUNTRY_NAME)
                .build();

        // When
        BankResponse response = bankMapper.toBankResponse(bank);

        // Then
        assertThat(response.getSwiftCode()).isEqualTo(BANK_SWIFT_CODE);
        assertThat(response.getBankName()).isEqualTo(BANK_NAME);
        assertThat(response.getAddress()).isEqualTo(BANK_ADDRESS);
        assertThat(response.getCountryISO2()).isEqualTo(BANK_COUNTRY_CODE);
        assertThat(response.getCountryName()).isEqualTo(BANK_COUNTRY_NAME);
    }

    @Test
    void toBankResponseNullCountryName_whenBankIsValid_mapsFieldsWithNullCountryName() {
        // Given
        Bank bank = Bank.builder()
                .swiftCode(BANK_SWIFT_CODE)
                .name(BANK_NAME)
                .address(BANK_ADDRESS)
                .countryIso2Code(BANK_COUNTRY_CODE)
                .countryName(BANK_COUNTRY_NAME)
                .build();

        // When
        BankResponse response = bankMapper.toBankResponseNullCountryName(bank);

        // Then
        assertThat(response.getSwiftCode()).isEqualTo(BANK_SWIFT_CODE);
        assertThat(response.getBankName()).isEqualTo(BANK_NAME);
        assertThat(response.getAddress()).isEqualTo(BANK_ADDRESS);
        assertThat(response.getCountryISO2()).isEqualTo(BANK_COUNTRY_CODE);
        assertThat(response.getCountryName()).isNull();
    }
}