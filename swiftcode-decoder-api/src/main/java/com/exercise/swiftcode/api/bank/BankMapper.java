package com.exercise.swiftcode.api.bank;

import com.exercise.swiftcode.api.bank.request.CreateBankRequest;
import com.exercise.swiftcode.api.bank.response.BankResponse;
import com.exercise.swiftcode.persistence.entity.Bank;
import org.springframework.stereotype.Component;

@Component
public class BankMapper {
    public Bank toBank(CreateBankRequest request) {
        return Bank.builder()
                .swiftCode(request.getSwiftCode())
                .name(request.getBankName())
                .address(request.getAddress())
                .countryIso2Code(request.getCountryISO2())
                .countryName(request.getCountryName())
                .build();
    }

    public BankResponse toBankResponse(Bank response) {
        return BankResponse.builder()
                .swiftCode(response.getSwiftCode())
                .bankName(response.getName())
                .address(response.getAddress())
                .countryISO2(response.getCountryIso2Code())
                .isHeadquarter(response.getSwiftCode().endsWith("XXX"))
                .countryName(response.getCountryName())
                .build();
    }

    public BankResponse toBankResponseNullCountryName(Bank response) {
        return BankResponse.builder()
                .swiftCode(response.getSwiftCode())
                .bankName(response.getName())
                .address(response.getAddress())
                .countryISO2(response.getCountryIso2Code())
                .isHeadquarter(response.getSwiftCode().endsWith("XXX"))
                .countryName(null)
                .build();
    }
}
