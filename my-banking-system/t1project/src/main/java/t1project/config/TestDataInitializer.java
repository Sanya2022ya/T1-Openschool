package t1project.config;

import t1project.model.Account;
import t1project.model.Client;
import t1project.model.Transaction;
import t1project.repository.AccountRepository;
import t1project.repository.ClientRepository;
import t1project.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class TestDataInitializer {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Bean
    public CommandLineRunner initTestData() {
        return args -> {
            for (int i = 0; i < 5; i++) {
                Client client = new Client();
                client.setFirstName("Имя" + i);
                client.setMiddleName("Отчество" + i);
                client.setLastName("Фамилия" + i);
                client.setClientId(UUID.randomUUID());

                client = clientRepository.save(client);

                for (int j = 0; j < 2; j++) {
                    Account account = new Account();
                    account.setClientId(client.getClientId());
                    account.setType(j % 2 == 0 ? Account.AccountType.DEBIT : Account.AccountType.CREDIT);
                    account.setBalance(BigDecimal.valueOf(new Random().nextInt(100_000)));

                    account = accountRepository.save(account);

                    for (int k = 0; k < 3; k++) {
                        Transaction transaction = new Transaction();
                        transaction.setAccountId(account.getId());
                        transaction.setAmount(BigDecimal.valueOf(new Random().nextInt(10_000)));
                        transaction.setTimestamp(LocalDateTime.now().minusDays(k));

                        transactionRepository.save(transaction);
                    }
                }
            }
        };
    }
}
