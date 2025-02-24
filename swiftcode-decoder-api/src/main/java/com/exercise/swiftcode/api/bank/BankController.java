package com.exercise.swiftcode.api.bank;

import com.exercise.swiftcode.api.bank.request.CreateBankRequest;
import com.exercise.swiftcode.api.bank.response.MessageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/swift-codes")
public class BankController {

    @PostMapping()
    @Operation(
            summary = "Add a new bank",
            description = "Adds new SWIFT code entries to the database for a specific country."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bank created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
    })
    public ResponseEntity<MessageResponse> createBank(@RequestBody CreateBankRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse(request.getBankName()));
    }
}