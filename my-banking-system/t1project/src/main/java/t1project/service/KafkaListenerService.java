package t1project.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.transaction_common_dto.TransactionAcceptDTO;
import t1project.dto.TransactionRequestDTO;
import com.example.transaction_common_dto.TransactionResultDTO;

import t1project.model.Account;
import t1project.model.Transaction;
import t1project.repository.AccountRepository;
import t1project.repository.TransactionRepository;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class KafkaListenerService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaProducerService kafkaProducerService;
    public KafkaListenerService(AccountRepository accountRepository, TransactionRepository transactionRepository, KafkaProducerService kafkaProducerService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.kafkaProducerService = kafkaProducerService;
    }
    @KafkaListener(topics = "t1_demo_metrics", groupId = "t1_group")
    public void listen(ConsumerRecord<String, String> record) {
        String topic = record.topic();
        String key = record.key();
        String message = record.value();

        String errorType = "UNKNOWN";
        Header header = record.headers().lastHeader("errorType");
        if (header != null) {
            errorType = new String(header.value(), StandardCharsets.UTF_8);
        }

        log.info("📥 Получено сообщение из Kafka:");
        log.info("▶️ Топик: {}", topic);
        log.info("🔑 Ключ: {}", key);
        log.info("📄 Значение: {}", message);
        log.info("⚠️ Тип ошибки: {}", errorType);

        if ("DATA_SOURCE".equalsIgnoreCase(errorType)) {
            log.warn("🛑 Это сообщение об ошибке источника данных!");
        } else if ("METRICS".equalsIgnoreCase(errorType)) {
            log.info("⏱ Это сообщение о метриках времени выполнения.");
        } else {
            log.info("ℹ️ Неизвестный тип ошибки.");
        }
    }
    @KafkaListener(
            topics = "t1_demo_transactions",
            groupId = "t1_group",
            containerFactory = "transactionResultKafkaListenerContainerFactory"
    )
    @Transactional
    public void processTransaction(TransactionRequestDTO request) {
        log.info("📥 Получено сообщение: {}", request);

        Optional<Account> optionalAccount = accountRepository.findById(request.getAccountId());

        if (optionalAccount.isEmpty()) {
            log.warn("❌ Аккаунт с ID {} не найден", request.getAccountId());
            return;
        }

        Account account = optionalAccount.get();

        if (account.getStatus() != Account.Status.OPEN) {
            log.warn("⛔ Аккаунт закрыт, транзакция отклонена.");
            return;
        }

        Transaction transaction = new Transaction();
        transaction.setAccountId(account.getId());
        transaction.setAmount(BigDecimal.valueOf(request.getAmount()));
        transaction.setStatus(Transaction.Status.REQUESTED);
        transaction = transactionRepository.save(transaction);

        account.setBalance(account.getBalance().add(BigDecimal.valueOf(request.getAmount())));
        accountRepository.save(account);

        TransactionAcceptDTO response = new TransactionAcceptDTO();
        response.setTransactionId(transaction.getTransactionId());
        response.setAccountId(account.getId());
        response.setClientId(account.getClientId());
        response.setAmount(BigDecimal.valueOf(request.getAmount()));
        response.setBalance(account.getBalance());
        response.setTimestamp(transaction.getTimestamp());

        kafkaProducerService.sendMessageObject("t1_demo_transaction_accept", "accepted", response);
        log.info("✅ Транзакция обработана и подтверждение отправлено.");
    }
    @KafkaListener(topics = "t1_demo_transaction_result", groupId = "t1_group", containerFactory = "transactionResultKafkaListenerContainerFactory")
    @Transactional
    public void handleTransactionResult(TransactionResultDTO result) {
        log.info("📩 Получен результат транзакции: {}", result);

        Optional<Transaction> optionalTransaction = transactionRepository.findByTransactionId(result.getTransactionId());
        if (optionalTransaction.isEmpty()) {
            log.warn("❌ Транзакция {} не найдена", result.getTransactionId());
            return;
        }

        Transaction transaction = optionalTransaction.get();

        Optional<Account> optionalAccount = accountRepository.findById(result.getAccountId());
        if (optionalAccount.isEmpty()) {
            log.warn("❌ Счёт {} не найден", result.getAccountId());
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
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
                accountRepository.save(account);
                log.info("❌ Транзакция {} отклонена, баланс скорректирован", transaction.getTransactionId());
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
