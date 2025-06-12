package t1project.service;

import com.example.transaction_common_dto.UnarrestAccountRequestDTO;
import com.example.transaction_common_dto.UnarrestAccountResponseDTO;
import com.example.transaction_common_dto.UnlockClientRequestDTO;
import com.example.transaction_common_dto.UnlockClientResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import t1project.model.Account;
import t1project.model.Client;
import t1project.repository.AccountRepository;
import t1project.repository.ClientRepository;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnblockingSchedulerService {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final WebClient unblockingServiceWebClient;

    private final Random random = new Random();


    private static final int CLIENTS_TO_PROCESS = 5; // N
    private static final int ACCOUNTS_TO_PROCESS = 10; // M

    /**
     * Запланированная задача для разблокировки клиентов.
     * Запускается каждые 30 секунд.
     */
    @Scheduled(fixedRateString = "${unblocking.scheduler.client.fixedRate:30000}")
    @Transactional
    public void processBlockedClients() {
        log.info("Starting scheduled task to process blocked clients...");

        List<Client> blockedClients = clientRepository.findByBlockedTrue();

        if (blockedClients.isEmpty()) {
            log.info("No blocked clients found to process.");
            return;
        }


        List<Client> clientsToUnblock = blockedClients.stream()
                .limit(CLIENTS_TO_PROCESS)
                .collect(Collectors.toList());

        if (clientsToUnblock.isEmpty()) {
            log.info("No clients selected for unblocking in this run.");
            return;
        }

        log.info("Found {} blocked clients, processing {} of them.", blockedClients.size(), clientsToUnblock.size());

        for (Client client : clientsToUnblock) {
            try {
                UnlockClientResponseDTO response = unblockingServiceWebClient.post()
                        .uri("/api/unblock/client")
                        .bodyValue(new UnlockClientRequestDTO(client.getClientId()))
                        .retrieve()
                        .bodyToMono(UnlockClientResponseDTO.class)
                        .block(java.time.Duration.ofSeconds(5));

                if (response != null && response.isUnlocked()) {
                    client.setBlocked(false);
                    clientRepository.save(client);
                    log.info("✅ Client {} successfully unblocked. Reason: {}", client.getClientId(), response.getReason());
                } else {
                    log.warn("❌ Failed to unblock client {}. Reason: {}", client.getClientId(), response != null ? response.getReason() : "No response from unblocking service.");
                }
            } catch (Exception e) {
                log.error("Error processing unblock request for client {}: {}", client.getClientId(), e.getMessage());
            }
        }
        log.info("Finished processing blocked clients.");
    }

    /**
     * Запланированная задача для снятия ареста со счетов.
     * Запускается каждые 30 секунд.
     */
    @Scheduled(fixedRateString = "${unblocking.scheduler.account.fixedRate:30000}") // 30 секунд
    @Transactional
    public void processArrestedAccounts() {
        log.info("Starting scheduled task to process arrested accounts...");

        List<Account> arrestedAccounts = accountRepository.findByStatus(Account.Status.ARRESTED);

        if (arrestedAccounts.isEmpty()) {
            log.info("No arrested accounts found to process.");
            return;
        }

        List<Account> accountsToUnarrest = arrestedAccounts.stream()
                .limit(ACCOUNTS_TO_PROCESS)
                .collect(Collectors.toList());

        if (accountsToUnarrest.isEmpty()) {
            log.info("No accounts selected for unarresting in this run.");
            return;
        }

        log.info("Found {} arrested accounts, processing {} of them.", arrestedAccounts.size(), accountsToUnarrest.size());

        for (Account account : accountsToUnarrest) {
            try {
                UnarrestAccountResponseDTO response = unblockingServiceWebClient.post()
                        .uri("/api/unblock/account")
                        .bodyValue(new UnarrestAccountRequestDTO(account.getAccountId()))
                        .retrieve()
                        .bodyToMono(UnarrestAccountResponseDTO.class)
                        .block(java.time.Duration.ofSeconds(5));

                if (response != null && response.isUnarrested()) {
                    account.setStatus(Account.Status.OPEN);
                    accountRepository.save(account);
                    log.info("✅ Account {} successfully unarrested. Reason: {}", account.getAccountId(), response.getReason());
                } else {
                    log.warn("❌ Failed to unarrest account {}. Reason: {}", account.getAccountId(), response != null ? response.getReason() : "No response from unblocking service.");
                }
            } catch (Exception e) {
                log.error("Error processing unarrest request for account {}: {}", account.getAccountId(), e.getMessage());
            }
        }
        log.info("Finished processing arrested accounts.");
    }
}