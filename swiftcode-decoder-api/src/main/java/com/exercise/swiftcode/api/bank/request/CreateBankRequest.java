package com.exercise.swiftcode.api.bank.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateBankRequest {
    @NotNull(message = "Address cannot be null")
    @NotBlank(message = "Address cannot be blank")
    @Schema(description = "Address of the bank", example = "UL. SWIETOJANSKA 15  BIALYSTOK, PODLASKIE, 15-277")
    String address;

    @NotNull(message = "Bank name cannot be null")
    @NotBlank(message = "Bank name cannot be blank")
    @Schema(description = "Name of the bank", example = "MBANK S.A. (FORMERLY BRE BANK S.A.)")
    String bankName;

    @NotNull(message = "Country ISO2 code cannot be null")
    @NotBlank(message = "Country ISO2 code cannot be blank")
    @Schema(description = "ISO2 code of the country for the bank", example = "PL")
    String countryISO2;

    @NotNull(message = "Country name cannot be null")
    @NotBlank(message = "Country name cannot be blank")
    @Schema(description = "Country name of the bank", example = "POLAND")
    String countryName;

    @NotNull(message = "The isHeadquarter flag must be provided")
    @Schema(description = "Is bank a HQ", example = "true")
    Boolean isHeadquarter;

    @NotNull(message = "SWIFT code cannot be null")
    @NotBlank(message = "SWIFT code cannot be blank")
    @Schema(description = "SWIFT code of the bank", example = "BREXPLPWXXX")
    String swiftCode;
}
