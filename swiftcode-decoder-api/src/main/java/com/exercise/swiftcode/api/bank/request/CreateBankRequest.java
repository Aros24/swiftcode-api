package com.exercise.swiftcode.api.bank.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateBankRequest {
    @Schema(description = "Address of the bank", example = "UL. SWIETOJANSKA 15  BIALYSTOK, PODLASKIE, 15-277")
    String address;

    @Schema(description = "Name of the bank", example = "MBANK S.A. (FORMERLY BRE BANK S.A.)")
    String bankName;

    @Schema(description = "ISO2 code of the country for the bank", example = "PL")
    String countryISO2;

    @Schema(description = "Country name of the bank", example = "POLAND")
    String countryName;

    @Schema(description = "Is bank a HQ", example = "true")
    Boolean isHeadquarter;

    @Schema(description = "SWIFT code of the bank", example = "BREXPLPWXXX")
    String swiftCode;
}
