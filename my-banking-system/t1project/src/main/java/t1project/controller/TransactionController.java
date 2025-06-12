package t1project.controller;

import org.springframework.http.ResponseEntity;
import t1project.dto.TransactionRequestDTO;
import t1project.model.Transaction; // Добавьте импорт Transaction
import t1project.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Добавьте импорт Slf4j
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j // Добавляем Slf4j для логирования
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/account/{accountId}")
    public List<Transaction> getByAccountId(@PathVariable Long accountId) {
        return transactionService.getByAccountId(accountId);
    }

    @PostMapping
    public ResponseEntity<String> createTransaction(@RequestBody TransactionRequestDTO request) {
        try {
            Transaction savedTransaction = transactionService.processNewTransactionRequest(request);
            if (savedTransaction.getStatus() == Transaction.Status.REJECTED) {
                return ResponseEntity.status(403).body("Транзакция отклонена (возможно, клиент заблокирован или недостаточно средств).");
            }
            return ResponseEntity.ok("Транзакция отправлена на обработку. ID: " + savedTransaction.getTransactionId());
        } catch (IllegalArgumentException e) {
            log.error("Ошибка при создании транзакции: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Внутренняя ошибка сервера при создании транзакции: {}", e.getMessage());
            return ResponseEntity.status(500).body("Внутренняя ошибка сервера.");
        }
    }
}