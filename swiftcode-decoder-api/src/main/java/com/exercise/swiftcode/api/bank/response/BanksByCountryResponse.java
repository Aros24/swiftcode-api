package com.exercise.swiftcode.api.bank.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BanksByCountryResponse {
    @Schema(description = "ISO2 code of the country", example = "PL")
    String countryISO2;

    @Schema(description = "Country name", example = "POLAND")
    String countryName;

    @Schema(description = "List of banks in the country with their SWIFT codes")
    @JsonProperty("swiftCodes")
    List<BankResponse> branches;
}
