package com.mybank.unblocking_service.service;

import com.example.transaction_common_dto.UnlockClientResponseDTO;
import com.example.transaction_common_dto.UnarrestAccountResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class UnblockingService {

    private final Random random = new Random();
    private static final int UNLOCK_SUCCESS_PERCENTAGE = 70;

    public UnlockClientResponseDTO processUnlockClientRequest(UUID clientId) {
        boolean unlocked = random.nextInt(100) < UNLOCK_SUCCESS_PERCENTAGE;
        String reason = unlocked ? "Клиент успешно разблокирован." : "Отказано в разблокировке клиента по внутренним причинам.";
        log.info("Decision for client {}: Unlocked = {}, Reason = {}", clientId, unlocked, reason);
        return new UnlockClientResponseDTO(clientId, unlocked, reason);
    }

    public UnarrestAccountResponseDTO processUnarrestAccountRequest(UUID accountId) {
        boolean unarrested = random.nextInt(100) < UNLOCK_SUCCESS_PERCENTAGE;
        String reason = unarrested ? "Арест со счета успешно снят." : "Отказано в снятии ареста со счета по внутренним причинам.";
        log.info("Decision for account {}: Unarrested = {}, Reason = {}", accountId, unarrested, reason);
        return new UnarrestAccountResponseDTO(accountId, unarrested, reason);
    }
}