package com.exercise.swiftcode.service.bank;

import com.exercise.swiftcode.api.bank.BankMapper;
import com.exercise.swiftcode.api.bank.BankValidator;
import com.exercise.swiftcode.api.bank.request.CreateBankRequest;
import com.exercise.swiftcode.api.bank.response.BankListResponse;
import com.exercise.swiftcode.api.bank.response.BankResponse;
import com.exercise.swiftcode.api.bank.response.BanksByCountryResponse;
import com.exercise.swiftcode.api.bank.response.MessageResponse;
import com.exercise.swiftcode.config.exceptions.BankNotFoundException;
import com.exercise.swiftcode.config.exceptions.DuplicateSwiftCodeException;
import com.exercise.swiftcode.config.exceptions.CountryCodeNotFoundException;
import com.exercise.swiftcode.config.exceptions.ValidationException;
import com.exercise.swiftcode.persistence.entity.Bank;
import com.exercise.swiftcode.persistence.entity.CountryCode;
import com.exercise.swiftcode.persistence.repository.BankRepository;
import com.exercise.swiftcode.persistence.repository.CountryCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankServiceTest {
    @InjectMocks
    private BankService bankService;

    @Mock
    private BankMapper bankMapper;

    @Mock
    private BankRepository bankRepository;

    @Mock
    private CountryCodeRepository countryCodeRepository;

    @Mock
    private BankValidator bankValidator;

    @Captor
    private ArgumentCaptor<Bank> bankCaptor;

    private CreateBankRequest validRequest;

    private static final String BANK_NAME = "Bank Name";
    private static final String BANK_ADDRESS = "Bank Address";
    private static final String BANK_COUNTRY_NAME = "Country Name";
    private static final String BANK_COUNTRY_CODE = "CN";
    private static final String BANK_SWIFTCODE_HQ = "TESTUS33XXX";
    private static final String BANK_SWIFTCODE_BRANCH = "TESTUS33111";
    private static final String BANK_SWIFTCODE_PREFIX = "TESTUS33";

    @BeforeEach
    void setUp() {
        validRequest = CreateBankRequest.builder()
                .swiftCode(BANK_SWIFTCODE_HQ)
                .isHeadquarter(true)
                .bankName(BANK_NAME)
                .address(BANK_ADDRESS)
                .countryISO2(BANK_COUNTRY_CODE)
                .countryName(BANK_COUNTRY_NAME)
                .build();
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

    private BankResponse createDefaultBankResponse() {
        return BankResponse.builder()
                .swiftCode(BANK_SWIFTCODE_HQ)
                .bankName(BANK_NAME)
                .address(BANK_ADDRESS)
                .countryISO2(BANK_COUNTRY_CODE)
                .countryName(BANK_COUNTRY_NAME)
                .build();
    }

    @Test
    void createBank_successfulCreation_returnsSuccessMessage() {
        // Given
        Bank bank = createDefaultBank();

        when(bankRepository.existsBySwiftCode(BANK_SWIFTCODE_HQ)).thenReturn(false);
        when(countryCodeRepository.findByCountryIso2Code(BANK_COUNTRY_CODE))
                .thenReturn(Optional.of(new CountryCode("1",BANK_COUNTRY_NAME,BANK_COUNTRY_CODE)));
        when(bankMapper.toBank(any(CreateBankRequest.class))).thenReturn(bank);
        when(bankRepository.save(any(Bank.class))).thenReturn(bank);

        // When
        MessageResponse response = bankService.createBank(validRequest);

        // Then
        assertNotNull(response);
        assertEquals("Bank successfully created.", response.getMessage());

        verify(bankValidator).validateCreateBankRequest(validRequest);
        verify(bankRepository).existsBySwiftCode(BANK_SWIFTCODE_HQ);
        verify(countryCodeRepository).findByCountryIso2Code(BANK_COUNTRY_CODE);
        verify(bankMapper).toBank(any(CreateBankRequest.class));
        verify(bankRepository).save(bankCaptor.capture());

        Bank capturedBank = bankCaptor.getValue();
        assertEquals(BANK_SWIFTCODE_HQ, capturedBank.getSwiftCode());
        assertEquals(BANK_NAME, capturedBank.getName());
        assertEquals(BANK_ADDRESS, capturedBank.getAddress());
        assertEquals(BANK_COUNTRY_CODE, capturedBank.getCountryIso2Code());
        assertEquals(BANK_COUNTRY_NAME, capturedBank.getCountryName());
    }

    @Test
    void createBank_duplicateSwiftCode_throwsDuplicateSwiftCodeException() {
        // Given
        when(bankRepository.existsBySwiftCode(BANK_SWIFTCODE_HQ)).thenReturn(true);

        // When & Then
        DuplicateSwiftCodeException exception = assertThrows(
                DuplicateSwiftCodeException.class,
                () -> bankService.createBank(validRequest)
        );

        assertEquals("SWIFT Code already exists.", exception.getMessage());

        verify(bankValidator).validateCreateBankRequest(validRequest);
        verify(bankRepository).existsBySwiftCode(BANK_SWIFTCODE_HQ);
        verify(countryCodeRepository, never()).findByCountryIso2Code(anyString());
        verify(bankMapper, never()).toBank(any(CreateBankRequest.class));
        verify(bankRepository, never()).save(any(Bank.class));
    }

    @Test
    void createBank_countryCodeNotFound_throwsCountryCodeNotFoundException() {
        // Given
        when(bankRepository.existsBySwiftCode(BANK_SWIFTCODE_HQ)).thenReturn(false);
        when(countryCodeRepository.findByCountryIso2Code(BANK_COUNTRY_CODE))
                .thenReturn(Optional.empty());

        // When & Then
        CountryCodeNotFoundException exception = assertThrows(
                CountryCodeNotFoundException.class,
                () -> bankService.createBank(validRequest)
        );

        assertEquals(String.format("Country ISO2 code '%s' does not exist.", BANK_COUNTRY_CODE), exception.getMessage());

        verify(bankValidator).validateCreateBankRequest(validRequest);
        verify(bankRepository).existsBySwiftCode(BANK_SWIFTCODE_HQ);
        verify(countryCodeRepository).findByCountryIso2Code(BANK_COUNTRY_CODE);
        verify(bankMapper, never()).toBank(any(CreateBankRequest.class));
        verify(bankRepository, never()).save(any(Bank.class));
    }

    @Test
    void deleteBank_nonHeadquarter_successfulDeletion_returnsSuccessMessage() {
        // Given
        when(bankRepository.existsBySwiftCode(BANK_SWIFTCODE_BRANCH)).thenReturn(true);

        // When
        MessageResponse response = bankService.deleteBank(BANK_SWIFTCODE_BRANCH);

        // Then
        assertNotNull(response);
        assertEquals("Bank successfully deleted.", response.getMessage());

        verify(bankValidator).validateDeleteBankRequest(BANK_SWIFTCODE_BRANCH);
        verify(bankRepository).existsBySwiftCode(BANK_SWIFTCODE_BRANCH);
        verify(bankRepository).deleteBySwiftCode(BANK_SWIFTCODE_BRANCH);
        verify(bankRepository, never()).deleteBySwiftCodeStartingWith(anyString());
    }

    @Test
    void deleteBank_headquarter_successfulDeletionWithBranches_returnsSuccessMessage() {
        // Given
        when(bankRepository.existsBySwiftCode(BANK_SWIFTCODE_HQ)).thenReturn(true);

        // When
        MessageResponse response = bankService.deleteBank(BANK_SWIFTCODE_HQ);

        // Then
        assertNotNull(response);
        assertEquals("Bank successfully deleted.", response.getMessage());

        verify(bankValidator).validateDeleteBankRequest(BANK_SWIFTCODE_HQ);
        verify(bankRepository).existsBySwiftCode(BANK_SWIFTCODE_HQ);
        verify(bankRepository).deleteBySwiftCodeStartingWith(BANK_SWIFTCODE_PREFIX);
        verify(bankRepository, never()).deleteBySwiftCode(anyString());
    }

    @Test
    void deleteBank_bankNotFound_throwsBankNotFoundException() {
        // Given
        when(bankRepository.existsBySwiftCode(BANK_SWIFTCODE_HQ)).thenReturn(false);

        // When & Then
        BankNotFoundException exception = assertThrows(
                BankNotFoundException.class,
                () -> bankService.deleteBank(BANK_SWIFTCODE_HQ)
        );

        assertEquals(String.format("Bank with SWIFT Code '%s' not found.",BANK_SWIFTCODE_HQ), exception.getMessage());

        verify(bankValidator).validateDeleteBankRequest(BANK_SWIFTCODE_HQ);
        verify(bankRepository).existsBySwiftCode(BANK_SWIFTCODE_HQ);
        verify(bankRepository, never()).deleteBySwiftCode(anyString());
        verify(bankRepository, never()).deleteBySwiftCodeStartingWith(anyString());
    }

    @Test
    void deleteBank_validationFails_throwsValidationException() {
        // Given
        doThrow(new ValidationException("Invalid SWIFT Code"))
                .when(bankValidator).validateDeleteBankRequest(BANK_SWIFTCODE_HQ);

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bankService.deleteBank(BANK_SWIFTCODE_HQ)
        );

        assertEquals("Invalid SWIFT Code", exception.getMessage());

        verify(bankValidator).validateDeleteBankRequest(BANK_SWIFTCODE_HQ);
        verify(bankRepository, never()).existsBySwiftCode(anyString());
        verify(bankRepository, never()).deleteBySwiftCode(anyString());
        verify(bankRepository, never()).deleteBySwiftCodeStartingWith(anyString());
    }

    @Test
    void getBankAndBranches_nonHeadquarter_successfulRetrieval_returnsBankListResponse() {
        // Given
        Bank branchBank = createDefaultBank().toBuilder()
                .swiftCode(BANK_SWIFTCODE_BRANCH)
                .build();

        BankResponse branchResponse = createDefaultBankResponse().toBuilder()
                        .swiftCode(BANK_SWIFTCODE_BRANCH)
                        .build();

        when(bankRepository.findBySwiftCode(BANK_SWIFTCODE_BRANCH))
                .thenReturn(Optional.of(branchBank));
        when(bankMapper.toBankResponse(branchBank)).thenReturn(branchResponse);

        // When
        BankListResponse response = bankService.getBankAndBranches(BANK_SWIFTCODE_BRANCH);

        // Then
        assertNotNull(response);
        assertEquals(BANK_ADDRESS, response.getAddress());
        assertEquals(BANK_NAME, response.getBankName());
        assertEquals(BANK_COUNTRY_CODE, response.getCountryIso2());
        assertEquals(BANK_COUNTRY_NAME, response.getCountryName());
        assertFalse(response.getIsHeadquarter());
        assertEquals(BANK_SWIFTCODE_BRANCH, response.getSwiftCode());
        assertTrue(response.getBranches().isEmpty());

        verify(bankValidator).validateSwiftCode(BANK_SWIFTCODE_BRANCH);
        verify(bankRepository).findBySwiftCode(BANK_SWIFTCODE_BRANCH);
        verify(bankMapper).toBankResponse(branchBank);
        verify(bankRepository, never()).findBySwiftCodeStartingWith(anyString());
    }

    @Test
    void getBankAndBranches_headquarter_successfulRetrievalWithBranches_returnsBankListResponse() {
        // Given
        Bank hqBank = createDefaultBank();
        Bank branchBank = createDefaultBank().toBuilder()
                .swiftCode(BANK_SWIFTCODE_BRANCH)
                .name(BANK_NAME + " Branch")
                .address(BANK_ADDRESS + " Branch")
                .build();

        BankResponse hqResponse = createDefaultBankResponse();
        BankResponse branchResponse = createDefaultBankResponse().toBuilder()
                .swiftCode(BANK_SWIFTCODE_BRANCH)
                .bankName(BANK_NAME + " Branch")
                .address(BANK_ADDRESS + " Branch")
                .countryName(null)
                .build();

        when(bankRepository.findBySwiftCode(BANK_SWIFTCODE_HQ))
                .thenReturn(Optional.of(hqBank));
        when(bankMapper.toBankResponse(hqBank)).thenReturn(hqResponse);
        when(bankRepository.findBySwiftCodeStartingWith(BANK_SWIFTCODE_PREFIX))
                .thenReturn(List.of(hqBank, branchBank));
        when(bankMapper.toBankResponseNullCountryName(branchBank)).thenReturn(branchResponse);

        // When
        BankListResponse response = bankService.getBankAndBranches(BANK_SWIFTCODE_HQ);

        // Then
        assertNotNull(response);
        assertEquals(BANK_ADDRESS, response.getAddress());
        assertEquals(BANK_NAME, response.getBankName());
        assertEquals(BANK_COUNTRY_CODE, response.getCountryIso2());
        assertEquals(BANK_COUNTRY_NAME, response.getCountryName());
        assertTrue(response.getIsHeadquarter());
        assertEquals(BANK_SWIFTCODE_HQ, response.getSwiftCode());
        assertEquals(1, response.getBranches().size());
        assertEquals(BANK_SWIFTCODE_BRANCH, response.getBranches().getFirst().getSwiftCode());
        assertNull(response.getBranches().getFirst().getCountryName());

        verify(bankValidator).validateSwiftCode(BANK_SWIFTCODE_HQ);
        verify(bankRepository).findBySwiftCode(BANK_SWIFTCODE_HQ);
        verify(bankMapper).toBankResponse(hqBank);
        verify(bankRepository).findBySwiftCodeStartingWith(BANK_SWIFTCODE_PREFIX);
        verify(bankMapper).toBankResponseNullCountryName(branchBank);
    }

    @Test
    void getBankAndBranches_bankNotFound_throwsBankNotFoundException() {
        // Given
        when(bankRepository.findBySwiftCode(BANK_SWIFTCODE_HQ))
                .thenReturn(Optional.empty());

        // When
        BankNotFoundException exception = assertThrows(
                BankNotFoundException.class,
                () -> bankService.getBankAndBranches(BANK_SWIFTCODE_HQ)
        );

        // Then
        assertEquals(String.format("Bank with SWIFT Code '%s' not found.",BANK_SWIFTCODE_HQ), exception.getMessage());
        verify(bankValidator).validateSwiftCode(BANK_SWIFTCODE_HQ);
        verify(bankRepository).findBySwiftCode(BANK_SWIFTCODE_HQ);
        verify(bankMapper, never()).toBankResponse(any(Bank.class));
        verify(bankRepository, never()).findBySwiftCodeStartingWith(anyString());
    }

    @Test
    void getBankAndBranches_validationFails_throwsValidationException() {
        // Given
        doThrow(new ValidationException("Invalid SWIFT Code"))
                .when(bankValidator).validateSwiftCode(BANK_SWIFTCODE_HQ);

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bankService.getBankAndBranches(BANK_SWIFTCODE_HQ)
        );

        assertEquals("Invalid SWIFT Code", exception.getMessage());

        verify(bankValidator).validateSwiftCode(BANK_SWIFTCODE_HQ);
        verify(bankRepository, never()).findBySwiftCode(anyString());
        verify(bankMapper, never()).toBankResponse(any(Bank.class));
        verify(bankRepository, never()).findBySwiftCodeStartingWith(anyString());
    }

    @Test
    void getBanksByIsoCode_validIsoCode_successfulRetrieval_returnsBanksByCountryResponse() {
        // Given
        CountryCode country = CountryCode.builder()
                .countryIso2Code(BANK_COUNTRY_CODE)
                .countryName(BANK_COUNTRY_NAME)
                .build();

        Bank bank1 = createDefaultBank();
        Bank bank2 = createDefaultBank().toBuilder()
                .name(BANK_NAME + " Branch")
                .address(BANK_ADDRESS + " Branch")
                .build();

        BankResponse response1 = createDefaultBankResponse().toBuilder()
                .countryName(null)
                .build();
        BankResponse response2 = createDefaultBankResponse().toBuilder()
                .swiftCode(BANK_SWIFTCODE_BRANCH)
                .bankName(BANK_NAME + " Branch")
                .address(BANK_ADDRESS + " Branch")
                .countryName(null)
                .build();

        when(countryCodeRepository.findByCountryIso2Code(BANK_COUNTRY_CODE))
                .thenReturn(Optional.of(country));
        when(bankRepository.findByCountryIso2Code(BANK_COUNTRY_CODE))
                .thenReturn(List.of(bank1, bank2));
        when(bankMapper.toBankResponseNullCountryName(bank1)).thenReturn(response1);
        when(bankMapper.toBankResponseNullCountryName(bank2)).thenReturn(response2);

        // When
        BanksByCountryResponse response = bankService.getBanksByIsoCode(BANK_COUNTRY_CODE);

        // Then
        assertNotNull(response);
        assertEquals(BANK_COUNTRY_CODE, response.getCountryISO2());
        assertEquals(BANK_COUNTRY_NAME, response.getCountryName());
        assertEquals(2, response.getBranches().size());
        assertEquals(BANK_SWIFTCODE_HQ, response.getBranches().get(0).getSwiftCode());
        assertEquals(BANK_SWIFTCODE_BRANCH, response.getBranches().get(1).getSwiftCode());
        assertNull(response.getBranches().get(0).getCountryName());
        assertNull(response.getBranches().get(1).getCountryName());

        verify(bankValidator).validateCountryIso2Length(BANK_COUNTRY_CODE);
        verify(countryCodeRepository).findByCountryIso2Code(BANK_COUNTRY_CODE);
        verify(bankRepository).findByCountryIso2Code(BANK_COUNTRY_CODE);
        verify(bankMapper).toBankResponseNullCountryName(bank1);
        verify(bankMapper).toBankResponseNullCountryName(bank2);
    }

    @Test
    void getBanksByIsoCode_countryCodeNotFound_throwsCountryCodeNotFoundException() {
        // Given
        when(countryCodeRepository.findByCountryIso2Code(BANK_COUNTRY_CODE))
                .thenReturn(Optional.empty());

        // When & Then
        CountryCodeNotFoundException exception = assertThrows(
                CountryCodeNotFoundException.class,
                () -> bankService.getBanksByIsoCode(BANK_COUNTRY_CODE)
        );

        assertEquals(String.format("Country ISO2 code '%s' does not exist.", BANK_COUNTRY_CODE), exception.getMessage());

        verify(bankValidator).validateCountryIso2Length(BANK_COUNTRY_CODE);
        verify(countryCodeRepository).findByCountryIso2Code(BANK_COUNTRY_CODE);
        verify(bankRepository, never()).findByCountryIso2Code(anyString());
        verify(bankMapper, never()).toBankResponseNullCountryName(any(Bank.class));
    }

    @Test
    void getBanksByIsoCode_noBanksFound_throwsBankNotFoundException() {
        // Given
        CountryCode country = CountryCode.builder()
                .countryIso2Code(BANK_COUNTRY_CODE)
                .countryName(BANK_COUNTRY_NAME)
                .build();

        when(countryCodeRepository.findByCountryIso2Code(BANK_COUNTRY_CODE))
                .thenReturn(Optional.of(country));
        when(bankRepository.findByCountryIso2Code(BANK_COUNTRY_CODE))
                .thenReturn(List.of());

        // When & Then
        BankNotFoundException exception = assertThrows(
                BankNotFoundException.class,
                () -> bankService.getBanksByIsoCode(BANK_COUNTRY_CODE)
        );

        assertEquals(String.format("No banks found for country code '%s'.", BANK_COUNTRY_CODE), exception.getMessage());

        verify(bankValidator).validateCountryIso2Length(BANK_COUNTRY_CODE);
        verify(countryCodeRepository).findByCountryIso2Code(BANK_COUNTRY_CODE);
        verify(bankRepository).findByCountryIso2Code(BANK_COUNTRY_CODE);
        verify(bankMapper, never()).toBankResponseNullCountryName(any(Bank.class));
    }

    @Test
    void getBanksByIsoCode_validationFails_throwsValidationException() {
        // Given
        doThrow(new ValidationException("Invalid country ISO2 code length"))
                .when(bankValidator).validateCountryIso2Length(BANK_COUNTRY_CODE);

        // When & Then
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bankService.getBanksByIsoCode(BANK_COUNTRY_CODE)
        );

        assertEquals("Invalid country ISO2 code length", exception.getMessage());

        verify(bankValidator).validateCountryIso2Length(BANK_COUNTRY_CODE);
        verify(countryCodeRepository, never()).findByCountryIso2Code(anyString());
        verify(bankRepository, never()).findByCountryIso2Code(anyString());
        verify(bankMapper, never()).toBankResponseNullCountryName(any(Bank.class));
    }
}