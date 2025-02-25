package com.exercise.swiftcode.api.bank.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankResponse {
    @Schema(description = "Address of the bank", example = "UL. SWIETOJANSKA 15 BIALYSTOK, PODLASKIE, 15-277")
    String address;

    @Schema(description = "Name of the bank", example = "MBANK S.A.")
    String bankName;

    @Schema(description = "ISO2 code of the country for the bank", example = "PL")
    String countryISO2;

    @Schema(description = "Country name of the bank", example = "POLAND")
    String countryName;

    @Schema(description = "Indicates if this is a headquarter", example = "false")
    Boolean isHeadquarter;

    @Schema(description = "SWIFT code of the bank", example = "BREXPLPW123")
    String swiftCode;
}
