package t1project.controller;

import org.springframework.http.ResponseEntity;
import t1project.dto.TransactionAcceptDTO;
import t1project.dto.TransactionRequestDTO;
import t1project.model.Transaction;
import t1project.repository.AccountRepository;
import t1project.repository.TransactionRepository;
import t1project.service.KafkaProducerService;
import t1project.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import t1project.model.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final KafkaProducerService kafkaProducerService;
    private final AccountRepository accountRepository;
    @GetMapping("/account/{accountId}")
    public List<Transaction> getByAccountId(@PathVariable Long accountId) {
        return transactionService.getByAccountId(accountId);
    }

    @PostMapping
    public ResponseEntity<String> createTransaction(@RequestBody TransactionRequestDTO request) {
        Optional<Account> optionalAccount = accountRepository.findById(request.getAccountId());
        if (optionalAccount.isEmpty()) {
            return ResponseEntity.badRequest().body("Аккаунт с ID " + request.getAccountId() + " не найден.");
        }
        Account account = optionalAccount.get();

        if (account.getStatus() != Account.Status.OPEN) {
            return ResponseEntity.badRequest().body("Аккаунт закрыт или заблокирован, транзакция отклонена.");
        }

        Transaction transaction = new Transaction();
        UUID transactionId = UUID.randomUUID();
        transaction.setTransactionId(transactionId);
        transaction.setAccountId(request.getAccountId());
        transaction.setAmount(BigDecimal.valueOf(request.getAmount()));
        transaction.setStatus(Transaction.Status.REQUESTED);
        transaction.setTimestamp(LocalDateTime.now());

        transactionService.createTransaction(transaction);

        account.setBalance(account.getBalance().add(BigDecimal.valueOf(request.getAmount())));
        accountRepository.save(account);

        TransactionAcceptDTO transactionAcceptDTO = new TransactionAcceptDTO();
        transactionAcceptDTO.setClientId(account.getClientId());
        transactionAcceptDTO.setAccountId(account.getId());
        transactionAcceptDTO.setTransactionId(transactionId);
        transactionAcceptDTO.setAmount(request.getAmount());
        transactionAcceptDTO.setTimestamp(transaction.getTimestamp());
        transactionAcceptDTO.setBalance(account.getBalance());

        kafkaProducerService.sendMessageObject("t1_demo_transaction_accept", transactionId.toString(), transactionAcceptDTO);

        return ResponseEntity.accepted().body("Транзакция сохранена и отправлена в Kafka");
    }
}
