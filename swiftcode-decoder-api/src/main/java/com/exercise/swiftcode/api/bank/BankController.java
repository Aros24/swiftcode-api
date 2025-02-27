package com.exercise.swiftcode.api.bank;

import com.exercise.swiftcode.api.bank.request.CreateBankRequest;
import com.exercise.swiftcode.api.bank.response.BankListResponse;
import com.exercise.swiftcode.api.bank.response.BanksByCountryResponse;
import com.exercise.swiftcode.api.bank.response.MessageResponse;

import com.exercise.swiftcode.config.exceptions.ErrorResponse;
import com.exercise.swiftcode.service.bank.BankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/swift-codes")
public class BankController {
    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping()
    @Operation(
            summary = "Add a new bank",
            description = "Adds new SWIFT code entries to the database for a specific country."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bank created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Duplicated Swift Code",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> createBank(@Valid @RequestBody CreateBankRequest request) {
        MessageResponse messageResponse = bankService.createBank(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageResponse);
    }

    @DeleteMapping("{swiftCode}")
    @Operation(
            summary = "Removes a bank",
            description = "Deletes swift-code data if swiftCode matches the one in the database."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank removed successfuly",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Bank not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> deleteBank(@PathVariable String swiftCode) {
        MessageResponse messageResponse = bankService.deleteBank(swiftCode);
        return ResponseEntity.ok(messageResponse);
    }

    @GetMapping("/{swiftCode}")
    @Operation(
            summary = "Get bank details",
            description = "Returns bank details based on the SWIFT code. If it's a headquarter, also returns branches."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved bank details",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BankListResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Bank not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BankListResponse> getBanksBySwiftCode(@PathVariable String swiftCode) {
        BankListResponse response = bankService.getBankAndBranches(swiftCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/country/{countryIso2}")
    @Operation(
            summary = "Get all SWIFT codes for a country",
            description = "Returns all SWIFT codes for a given country ISO2 code."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved SWIFT codes for the country",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BanksByCountryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No banks found for the country",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BanksByCountryResponse> getBanksByIsoCode(@PathVariable String countryIso2) {
        BanksByCountryResponse response = bankService.getBanksByIsoCode(countryIso2);
        return ResponseEntity.ok(response);
    }
}