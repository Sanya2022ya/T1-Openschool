package t1project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t1project.model.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(Long accountId);
    Optional<Transaction> findByTransactionId(UUID transactionId);
    List<Transaction> findAllByAccountIdAndStatus(Long accountId, Transaction.Status status);


}
