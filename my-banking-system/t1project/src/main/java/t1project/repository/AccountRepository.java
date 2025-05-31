package t1project.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import t1project.model.Account;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByClientId(java.util.UUID clientId);
}
