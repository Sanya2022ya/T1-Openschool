package t1project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t1project.model.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(Long accountId);
}
