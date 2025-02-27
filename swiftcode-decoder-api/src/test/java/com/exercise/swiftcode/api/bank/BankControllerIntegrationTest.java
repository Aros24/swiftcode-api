package com.exercise.swiftcode.api.bank;

import com.exercise.swiftcode.api.bank.request.CreateBankRequest;
import com.exercise.swiftcode.persistence.entity.Bank;
import com.exercise.swiftcode.persistence.entity.CountryCode;
import com.exercise.swiftcode.persistence.repository.BankRepository;
import com.exercise.swiftcode.persistence.repository.CountryCodeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BankControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private CountryCodeRepository countryCodeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/v1/swift-codes";
    private static final String BANK_SWIFTCODE_HQ = "TESTUS33XXX";
    private static final String BANK_SWIFTCODE_BRANCH = "TESTUS33ABC";
    private static final String BANK_COUNTRY_CODE = "CN";
    private static final String BANK_COUNTRY_NAME = "COUNTRY NAME";
    private static final String BANK_NAME = "Bank Name";
    private static final String BANK_ADDRESS = "Bank Address";

    @BeforeEach
    void setUp() {
        bankRepository.deleteAll();
        countryCodeRepository.deleteAll();

        CountryCode country = CountryCode.builder()
            .countryIso2Code(BANK_COUNTRY_CODE)
            .countryName(BANK_COUNTRY_NAME)
            .build();
        countryCodeRepository.save(country);
    }

    private Bank createDefaultBank() {
        return Bank.builder()
                .swiftCode(BANK_SWIFTCODE_HQ)
                .name(BANK_NAME)
                .address(BANK_ADDRESS)
                .countryIso2Code(BANK_COUNTRY_CODE)
                .countryName(BANK_COUNTRY_NAME)
                .build();
    }

    private CreateBankRequest createDefaultCreateBankRequest() {
        return CreateBankRequest.builder()
                .swiftCode(BANK_SWIFTCODE_HQ)
                .isHeadquarter(true)
                .bankName(BANK_NAME)
                .address(BANK_ADDRESS)
                .countryISO2(BANK_COUNTRY_CODE)
                .countryName(BANK_COUNTRY_NAME)
                .build();
    }

    @Test
    void createBank_validRequest_returnsCreatedStatusAndMessage() throws Exception {
        // Given
        CreateBankRequest request = createDefaultCreateBankRequest();

        // When & Then
        mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("Bank successfully created."));
    }

    @Test
    void createBank_duplicateSwiftCode_returnsConflict() throws Exception {
        // Given
        Bank bank = createDefaultBank();
        bankRepository.save(bank);
        CreateBankRequest request = createDefaultCreateBankRequest();

        // When & Then
        mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("SWIFT Code already exists."));
    }

    @Test
    void createBank_givenEmptyRequest_whenPost_thenReturnsBadRequest() throws Exception {
        // Given
        String emptyRequest = "{}";

        // When & Then
        mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(emptyRequest))
            .andExpect(status().isBadRequest())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("Country ISO2 code cannot be null")))
            .andExpect(jsonPath("$.message").value(containsString("SWIFT code cannot be null")))
            .andExpect(jsonPath("$.message").value(containsString("Bank name cannot be null")))
            .andExpect(jsonPath("$.message").value(containsString("Address cannot be null")))
            .andExpect(jsonPath("$.message").value(containsString("Country name cannot be null")))
            .andExpect(jsonPath("$.message").value(containsString("The isHeadquarter flag must be provided")));
    }

    @Test
    void createBank_givenInvalidSwiftCode_whenPost_thenReturnsBadRequest() throws Exception {
        // Given
        CreateBankRequest request = createDefaultCreateBankRequest().toBuilder()
                .swiftCode("SHORT")
                .build();

        // When & Then
        mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("SWIFT Code must be exactly 11 characters long."));
    }

    @Test
    void createBank_givenMissingBankName_whenPost_thenReturnsBadRequest() throws Exception {
        // Given
        CreateBankRequest request = createDefaultCreateBankRequest().toBuilder()
                .bankName(null)
                .build();

        // When & Then
        mockMvc.perform(post(BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("Bank name cannot be null")))
            .andExpect(jsonPath("$.message").value(containsString("Bank name cannot be blank")));
    }

    @Test
    void deleteBank_givenExistingBank_whenDelete_thenReturnsOk() throws Exception {
        // Given
        Bank bank = createDefaultBank();
        bankRepository.save(bank);

        // When & Then
        mockMvc.perform(delete(BASE_URL + "/{swiftCode}", BANK_SWIFTCODE_HQ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Bank successfully deleted."));
    }

    @Test
    void deleteBank_givenNonExistentBank_whenDelete_thenReturnsNotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete(BASE_URL + "/{swiftCode}", BANK_SWIFTCODE_HQ))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Bank with SWIFT Code 'TESTUS33XXX' not found."));
    }

    @Test
    void deleteBank_givenWrongSwiftCodeSize_whenDelete_thenReturnsBadRequest() throws Exception {
        // Given
        String wrongSwiftCode = "ABDEFG";
        // When & Then
        mockMvc.perform(delete(BASE_URL + "/{swiftCode}", wrongSwiftCode))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("SWIFT Code must be exactly 11 characters long."));
    }

    @Test
    void deleteBank_givenNoSwiftCode_whenDelete_thenReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(delete(BASE_URL))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("Request method 'DELETE' is not supported")));
    }

    @Test
    void whenWrongResource_thenReturnsNotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete(BASE_URL + "/"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("No static resource found on path")));
    }

    @Test
    void getBankAndBranches_givenExistingHeadquarterWithBranch_whenGet_thenReturnsOk() throws Exception {
        // Given
        Bank hqBank = createDefaultBank();
        Bank branchBank = createDefaultBank().toBuilder()
                .swiftCode(BANK_SWIFTCODE_BRANCH)
                .name(BANK_NAME + " Branch")
                .address(BANK_ADDRESS + " Branch")
                .build();
        bankRepository.save(hqBank);
        bankRepository.save(branchBank);

        // When & Then
        mockMvc.perform(get(BASE_URL + "/{swiftCode}", BANK_SWIFTCODE_HQ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.swiftCode").value(BANK_SWIFTCODE_HQ))
            .andExpect(jsonPath("$.bankName").value(BANK_NAME))
            .andExpect(jsonPath("$.address").value(BANK_ADDRESS))
            .andExpect(jsonPath("$.countryISO2").value(BANK_COUNTRY_CODE))
            .andExpect(jsonPath("$.countryName").value(BANK_COUNTRY_NAME))
            .andExpect(jsonPath("$.isHeadquarter").value(true))
            .andExpect(jsonPath("$.branches[0].swiftCode").value(BANK_SWIFTCODE_BRANCH))
            .andExpect(jsonPath("$.branches[0].countryName").doesNotExist());
    }

    @Test
    void getBankAndBranches_givenExistingHeadquarterWithoutBranch_whenGet_thenReturnsOk() throws Exception {
        // Given
        Bank hqBank = createDefaultBank();
        bankRepository.save(hqBank);

        // When & Then
        mockMvc.perform(get(BASE_URL + "/{swiftCode}", BANK_SWIFTCODE_HQ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.swiftCode").value(BANK_SWIFTCODE_HQ))
            .andExpect(jsonPath("$.bankName").value(BANK_NAME))
            .andExpect(jsonPath("$.address").value(BANK_ADDRESS))
            .andExpect(jsonPath("$.countryISO2").value(BANK_COUNTRY_CODE))
            .andExpect(jsonPath("$.countryName").value(BANK_COUNTRY_NAME))
            .andExpect(jsonPath("$.isHeadquarter").value(true))
            .andExpect(jsonPath("$.branches[0].swiftCode").doesNotExist());
    }

    @Test
    void getBankAndBranches_givenNonExistentBank_whenGet_thenReturnsNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get(BASE_URL + "/{swiftCode}", BANK_SWIFTCODE_HQ))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Bank with SWIFT Code 'TESTUS33XXX' not found."));
    }

    @Test
    void getBanksByIsoCode_givenExistingBanks_whenGet_thenReturnsOk() throws Exception {
        // Given
        Bank bank1 = createDefaultBank();
        Bank bank2 = createDefaultBank().toBuilder()
                .swiftCode(BANK_SWIFTCODE_BRANCH)
                .name(BANK_NAME + " Branch")
                .address(BANK_ADDRESS + " Branch")
                .build();
        bankRepository.save(bank1);
        bankRepository.save(bank2);

        // When & Then
        mockMvc.perform(get(BASE_URL + "/country/{countryIso2}", BANK_COUNTRY_CODE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.countryISO2").value(BANK_COUNTRY_CODE))
            .andExpect(jsonPath("$.countryName").value(BANK_COUNTRY_NAME))
            .andExpect(jsonPath("$.swiftCodes[0].swiftCode").value(BANK_SWIFTCODE_HQ))
            .andExpect(jsonPath("$.swiftCodes[0].countryName").doesNotExist())
            .andExpect(jsonPath("$.swiftCodes[1].swiftCode").value(BANK_SWIFTCODE_BRANCH))
            .andExpect(jsonPath("$.swiftCodes[1].countryName").doesNotExist());
    }

    @Test
    void getBanksByIsoCode_givenNoBanks_whenGet_thenReturnsNotFound() throws Exception {
        // When
        mockMvc.perform(get(BASE_URL + "/country/{countryIso2}", BANK_COUNTRY_CODE))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("No banks found for country code 'CN'."));
    }

    @Test
    void getBanksByIsoCode_givenInvalidCountryCode_whenGet_thenReturnsNotFound() throws Exception {
        // When
        mockMvc.perform(get(BASE_URL + "/country/{countryIso2}", "XX"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Country ISO2 code 'XX' does not exist."));
    }

    @Test
    void getBanksByIsoCode_givenInvalidCountryCodeLength_whenGet_thenReturnsBadRequest() throws Exception {
        // Given
        String invalidCountryCode = "USA";

        // When & Then
        mockMvc.perform(get(BASE_URL + "/country/{countryIso2}", invalidCountryCode))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Country ISO2 code must be exactly 2 characters long."));
    }
}
