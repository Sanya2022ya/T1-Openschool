package com.mybank.unblocking_service.controller;

import com.example.transaction_common_dto.UnlockClientRequestDTO;
import com.example.transaction_common_dto.UnlockClientResponseDTO;
import com.example.transaction_common_dto.UnarrestAccountRequestDTO;
import com.example.transaction_common_dto.UnarrestAccountResponseDTO;
import com.mybank.unblocking_service.service.UnblockingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/unblock")
@RequiredArgsConstructor
@Slf4j
public class UnblockingController {

    private final UnblockingService unblockingService;

    @PostMapping("/client")
    public ResponseEntity<UnlockClientResponseDTO> unblockClient(@RequestBody UnlockClientRequestDTO request) {
        log.info("Received unblock client request for ClientId: {}", request.getClientId());
        UnlockClientResponseDTO response = unblockingService.processUnlockClientRequest(request.getClientId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/account")
    public ResponseEntity<UnarrestAccountResponseDTO> unarrestAccount(@RequestBody UnarrestAccountRequestDTO request) {
        log.info("Received unarrest account request for AccountId: {}", request.getAccountId());
        UnarrestAccountResponseDTO response = unblockingService.processUnarrestAccountRequest(request.getAccountId());
        return ResponseEntity.ok(response);
    }
}