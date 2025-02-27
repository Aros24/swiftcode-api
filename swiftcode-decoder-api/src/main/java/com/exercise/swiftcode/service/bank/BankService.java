package com.exercise.swiftcode.service.bank;

import com.exercise.swiftcode.api.bank.BankMapper;
import com.exercise.swiftcode.api.bank.BankValidator;
import com.exercise.swiftcode.api.bank.request.CreateBankRequest;
import com.exercise.swiftcode.api.bank.response.BankListResponse;
import com.exercise.swiftcode.api.bank.response.BankResponse;
import com.exercise.swiftcode.api.bank.response.BanksByCountryResponse;
import com.exercise.swiftcode.api.bank.response.MessageResponse;
import com.exercise.swiftcode.config.exceptions.BankNotFoundException;
import com.exercise.swiftcode.config.exceptions.CountryCodeNotFoundException;
import com.exercise.swiftcode.config.exceptions.DuplicateSwiftCodeException;
import com.exercise.swiftcode.persistence.entity.Bank;
import com.exercise.swiftcode.persistence.entity.CountryCode;
import com.exercise.swiftcode.persistence.repository.BankRepository;
import com.exercise.swiftcode.persistence.repository.CountryCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BankService {
    private static final Logger logger = LoggerFactory.getLogger(BankService.class);

    private final BankMapper bankMapper;
    private final BankRepository bankRepository;
    private final CountryCodeRepository countryCodeRepository;
    private final BankValidator bankValidator;

    private static final String HEADQUARTER_SUFFIX = "XXX";
    private static final int SWIFT_PREFIX_LENGTH = 8;

    public BankService(BankMapper bankMapper, BankRepository bankRepository, CountryCodeRepository countryCodeRepository, BankValidator bankValidator) {
        this.bankMapper = bankMapper;
        this.bankRepository = bankRepository;
        this.countryCodeRepository = countryCodeRepository;
        this.bankValidator = bankValidator;
    }

    public MessageResponse createBank(CreateBankRequest request) {
        logger.info("Attempting to create bank with {}", request);
        bankValidator.validateCreateBankRequest(request);

        CreateBankRequest normalizedRequest = normalizeRequest(request);

        if (bankRepository.existsBySwiftCode(normalizedRequest.getSwiftCode())) {
            throw new DuplicateSwiftCodeException("SWIFT Code already exists.");
        }

        countryCodeRepository.findByCountryIso2Code(normalizedRequest.getCountryISO2())
                .orElseThrow(() -> new CountryCodeNotFoundException("Country ISO2 code '" + normalizedRequest.getCountryISO2() + "' does not exist."));

        Bank bank = bankMapper.toBank(normalizedRequest);
        bankRepository.save(bank);

        return new MessageResponse("Bank successfully created.");
    }

    public MessageResponse deleteBank(String swiftCode) {
        logger.info("Attempting to delete bank with SWIFT Code: {}", swiftCode);
        bankValidator.validateDeleteBankRequest(swiftCode);

        String normalizedSwiftCode = swiftCode.toUpperCase();
        if (!bankRepository.existsBySwiftCode(normalizedSwiftCode)) {
            throw new BankNotFoundException("Bank with SWIFT Code '" + normalizedSwiftCode + "' not found.");
        }

        if (normalizedSwiftCode.endsWith(HEADQUARTER_SUFFIX)) {
            String prefix = normalizedSwiftCode.substring(0, SWIFT_PREFIX_LENGTH);
            bankRepository.deleteBySwiftCodeStartingWith(prefix);
            logger.info("Successfully deleted headquarters and all associated branches with prefix: {}", prefix);
        } else {
            bankRepository.deleteBySwiftCode(normalizedSwiftCode);
            logger.info("Successfully deleted bank with SWIFT Code: {}", normalizedSwiftCode);
        }
        return new MessageResponse("Bank successfully deleted.");
    }

    public BankListResponse getBankAndBranches(String swiftCode) {
        logger.info("Attempting to get banks with SWIFT Code: {}", swiftCode);
        bankValidator.validateSwiftCode(swiftCode);

        String normalizedSwiftCode = swiftCode.toUpperCase();
        Bank bank = bankRepository.findBySwiftCode(normalizedSwiftCode)
                .orElseThrow(() -> new BankNotFoundException("Bank with SWIFT Code '" + normalizedSwiftCode + "' not found."));

        boolean isHeadquarter = normalizedSwiftCode.endsWith(HEADQUARTER_SUFFIX);
        BankResponse bankResponse = bankMapper.toBankResponse(bank);

        List<BankResponse> branchResponses = isHeadquarter
                ? bankRepository.findBySwiftCodeStartingWith(normalizedSwiftCode.substring(0, SWIFT_PREFIX_LENGTH))
                .stream()
                .filter(b -> !b.getSwiftCode().endsWith(HEADQUARTER_SUFFIX))
                .map(bankMapper::toBankResponseNullCountryName)
                .toList()
                : new ArrayList<>();

        return new BankListResponse(
                bankResponse.getAddress(),
                bankResponse.getBankName(),
                bankResponse.getCountryISO2(),
                bankResponse.getCountryName(),
                isHeadquarter,
                bankResponse.getSwiftCode(),
                branchResponses
        );
    }


    public BanksByCountryResponse getBanksByIsoCode(String countryISO2) {
        logger.info("Attempting to get bank with country ISO2: {}", countryISO2);
        bankValidator.validateCountryIso2Length(countryISO2);

        String normalizedIso2Code = countryISO2.toUpperCase();
        CountryCode country = countryCodeRepository.findByCountryIso2Code(normalizedIso2Code)
                .orElseThrow(() -> new CountryCodeNotFoundException("Country ISO2 code '" + normalizedIso2Code + "' does not exist."));

        List<Bank> banks = bankRepository.findByCountryIso2Code(normalizedIso2Code);
        if (banks.isEmpty()) {
            throw new BankNotFoundException("No banks found for country code '" + normalizedIso2Code + "'.");
        }

        List<BankResponse> bankResponses = banks.stream()
                .map(bankMapper::toBankResponseNullCountryName)
                .toList();

        return new BanksByCountryResponse(normalizedIso2Code, country.getCountryName(), bankResponses);
    }

    private CreateBankRequest normalizeRequest(CreateBankRequest request) {
        return  request.toBuilder()
            .countryISO2(request.getCountryISO2().toUpperCase())
            .countryName(request.getCountryName().toUpperCase())
            .swiftCode(request.getSwiftCode().toUpperCase())
            .build();
    }
}
