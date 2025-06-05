package t1project.service;

import com.example.transaction_common_dto.TransactionResultDTO;
import com.mybank.starter.aspects.aspect.Cached;
import com.mybank.starter.aspects.aspect.LogDataSourceError;
import com.example.transaction_common_dto.TransactionAcceptDTO;
import t1project.dto.TransactionRequestDTO;
import t1project.model.Account;
import t1project.model.Client;
import com.mybank.common.kafka.service.KafkaProducerService;
import t1project.model.Transaction;
import t1project.repository.AccountRepository;
import t1project.repository.ClientRepository;
import t1project.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.transaction_common_dto.ClientAccountCheckRequestDTO;
import com.example.transaction_common_dto.ClientAccountCheckResponseDTO;
import org.springframework.http.HttpHeaders;
import java.util.Base64;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final KafkaProducerService kafkaProducerService;
    private final WebClient.Builder webClientBuilder;

    @Value("${client.rejected-transaction-limit}")
    private int rejectedTransactionLimit;

    @Cached
    public List<Transaction> getByAccountId(Long accountId) {
        System.out.println("[CACHE MISS] Загрузка транзакций для аккаунта " + accountId + " из БД");
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);
        System.out.println("[LOADED] Загружено " + transactions.size() + " транзакций");
        return transactions;
    }

    @Transactional
    public Transaction processNewTransactionRequest(TransactionRequestDTO request) {
        Optional<Account> optionalAccount = accountRepository.findById(request.getAccountId());
        if (optionalAccount.isEmpty()) {
            log.error("Аккаунт с ID {} не найден.", request.getAccountId());
            throw new IllegalArgumentException("Аккаунт не найден.");
        }
        Account account = optionalAccount.get();

        UUID clientId = account.getClientId();

        ClientAccountCheckResponseDTO clientCheckResponse = checkClientWithService2(clientId, account.getId());

        if (clientCheckResponse.getStatus() == ClientAccountCheckResponseDTO.ClientStatus.BLOCKED) {
            account.setStatus(Account.Status.BLOCKED);
            accountRepository.save(account);
            log.warn("🚨 Клиент {} заблокирован по проверке Service 2. Аккаунт {} установлен в BLOCKED.", clientId, account.getId());

            Transaction transaction = new Transaction();
            transaction.setAccountId(account.getId());
            transaction.setAmount(BigDecimal.valueOf(request.getAmount()));
            transaction.setTimestamp(LocalDateTime.now());
            transaction.setStatus(Transaction.Status.REJECTED);
            return transactionRepository.save(transaction);
        }

        if (account.getStatus() != Account.Status.OPEN) {
            log.warn("Счет не находится в статусе OPEN для аккаунта {}.", account.getId());
            Transaction transaction = new Transaction();
            transaction.setAccountId(account.getId());
            transaction.setAmount(BigDecimal.valueOf(request.getAmount()));
            transaction.setTimestamp(LocalDateTime.now());
            transaction.setStatus(Transaction.Status.REJECTED);
            return transactionRepository.save(transaction);
        }

        if (account.getBalance().compareTo(BigDecimal.valueOf(request.getAmount())) < 0) {
            log.warn("Недостаточно средств на счете {} для транзакции на сумму {}.", account.getId(), request.getAmount());
            Transaction transaction = new Transaction();
            transaction.setAccountId(request.getAccountId());
            transaction.setAmount(BigDecimal.valueOf(request.getAmount()));
            transaction.setStatus(Transaction.Status.REJECTED);
            transaction.setTimestamp(LocalDateTime.now());
            Transaction savedTransaction = transactionRepository.save(transaction);

            kafkaProducerService.sendTransactionResult(new TransactionResultDTO(
                    savedTransaction.getTransactionId(),
                    clientId,
                    account.getId(),
                    "REJECTED",
                    "Недостаточно средств на счете"
            ));
            return savedTransaction;
        }

        Transaction transaction = new Transaction();
        transaction.setAccountId(request.getAccountId());
        transaction.setAmount(BigDecimal.valueOf(request.getAmount()));
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(Transaction.Status.REQUESTED);
        Transaction savedTransaction = transactionRepository.save(transaction);

        kafkaProducerService.sendTransactionAccept(new TransactionAcceptDTO(
                clientId, account.getId(), savedTransaction.getTransactionId(),
                LocalDateTime.now(), savedTransaction.getAmount(), "REQUESTED", account.getBalance()
        ));

        log.info("Транзакция {} для счета {} отправлена на обработку.", savedTransaction.getTransactionId(), account.getId());
        return savedTransaction;
    }

    private ClientAccountCheckResponseDTO checkClientWithService2(UUID clientId, Long accountId) {
        try {
            String auth = "service1:password123";
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            String authHeader = "Basic " + encodedAuth;

            WebClient webClient = webClientBuilder.baseUrl("http://transaction-analysis-app:8081").build();
            ClientAccountCheckResponseDTO response = webClient.post()
                    .uri("/api/client-check")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .bodyValue(new ClientAccountCheckRequestDTO(clientId, accountId))
                    .retrieve()
                    .bodyToMono(ClientAccountCheckResponseDTO.class)
                    .block();
            log.info("Получен ответ от Сервиса 2 о проверке клиента: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Ошибка при связи с Сервисом 2 для проверки клиента {}: {}", clientId, e.getMessage());
            return new ClientAccountCheckResponseDTO(ClientAccountCheckResponseDTO.ClientStatus.ACTIVE, "Service 2 is unreachable, defaulting to active.");
        }
    }

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @KafkaListener(topics = "t1_demo_transaction_result", groupId = "t1_transaction_group",
            containerFactory = "transactionResultKafkaListenerContainerFactory")
    @Transactional
    public void handleTransactionResult(TransactionResultDTO result) {
        log.info("📊 Получен результат обработки транзакции: {}", result);

        Optional<Transaction> optionalTransaction = transactionRepository.findByTransactionId(result.getTransactionId());
        if (optionalTransaction.isEmpty()) {
            log.warn("⚠️ Транзакция с ID {} не найдена, возможно, уже обработана или не существует.", result.getTransactionId());
            return;
        }
        Transaction transaction = optionalTransaction.get();

        Optional<Account> optionalAccount = accountRepository.findById(result.getAccountId());
        if (optionalAccount.isEmpty()) {
            log.error("❌ Аккаунт с ID {} не найден для транзакции {}. Невозможно обработать результат.", result.getAccountId(), result.getTransactionId());
            return;
        }
        Account account = optionalAccount.get();

        switch (result.getStatus()) {
            case "ACCEPTED" -> {
                transaction.setStatus(Transaction.Status.ACCEPTED);
                transactionRepository.save(transaction);
                log.info("✅ Транзакция {} подтверждена", transaction.getTransactionId());
            }

            case "REJECTED" -> {
                transaction.setStatus(Transaction.Status.REJECTED);
                transactionRepository.save(transaction);
                log.info("❌ Транзакция {} отклонена. Причина: {}", transaction.getTransactionId(), result.getReason());

                List<Transaction> rejectedTransactions = transactionRepository
                        .findAllByAccountIdAndStatus(account.getId(), Transaction.Status.REJECTED);

                if (rejectedTransactions.size() >= rejectedTransactionLimit) {
                    account.setStatus(Account.Status.ARRESTED);
                    accountRepository.save(account);
                    log.warn("🚨 Счет {} клиента {} переведен в статус ARRESTED из-за {} отклоненных транзакций (лимит: {}).",
                            account.getId(), account.getClientId(), rejectedTransactions.size(), rejectedTransactionLimit);
                }
            }

            case "BLOCKED" -> {
                List<Transaction> requestedTransactions = transactionRepository
                        .findAllByAccountIdAndStatus(account.getId(), Transaction.Status.REQUESTED);

                BigDecimal frozenSum = BigDecimal.ZERO;

                for (Transaction t : requestedTransactions) {
                    t.setStatus(Transaction.Status.BLOCKED);
                    transactionRepository.save(t);
                    frozenSum = frozenSum.add(t.getAmount());
                }

                account.setStatus(Account.Status.BLOCKED);
                account.setFrozenAmount(frozenSum);
                accountRepository.save(account);

                log.info("🧊 Счёт {} заблокирован, заморожено: {}", account.getId(), frozenSum);
            }

            default -> log.warn("⚠️ Неизвестный статус транзакции: {}", result.getStatus());
        }
    }
}