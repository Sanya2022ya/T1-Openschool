package t1project.service;

import t1project.aspect.Cached;
import t1project.aspect.LogDataSourceError;
import t1project.model.Transaction;
import t1project.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Cached
    public List<Transaction> getByAccountId(Long accountId) {
        System.out.println("[CACHE MISS] Загрузка транзакций для аккаунта " + accountId + " из БД");

        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

        System.out.println("[LOADED] Загружено " + transactions.size() + " транзакций");
        return transactions;
    }

    public Transaction createTransaction(Transaction transaction) {
        transaction.setTimestamp(java.time.LocalDateTime.now());
        return transactionRepository.save(transaction);
    }
}

