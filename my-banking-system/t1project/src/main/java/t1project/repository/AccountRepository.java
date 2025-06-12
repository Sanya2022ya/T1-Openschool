package t1project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import t1project.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByClientId(UUID clientId);
    Optional<Account> findByAccountId(UUID accountId);
    List<Account> findByStatus(Account.Status status);
    long countByStatus(Account.Status status);
}