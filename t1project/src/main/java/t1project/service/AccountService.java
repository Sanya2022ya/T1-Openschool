package t1project.service;

import t1project.aspect.LogDataSourceError;
import t1project.aspect.Metric;
import t1project.model.Account;
import t1project.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Metric
    public List<Account> getAccountsByClientId(UUID clientId) {
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return accountRepository.findByClientId(clientId);
    }

        public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }
}
