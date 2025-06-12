package t1project.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import t1project.model.Account;
import t1project.repository.AccountRepository;
import t1project.repository.ClientRepository;

import jakarta.annotation.PostConstruct; // Используйте jakarta.annotation.PostConstruct

@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

    private final MeterRegistry meterRegistry;
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;

    private long blockedClientsCount = 0;
    private long arrestedAccountsCount = 0;


    @PostConstruct
    public void registerMetrics() {
        Gauge.builder("mybank.clients.blocked.count", this, MetricsConfig::getBlockedClientsCount)
                .description("Number of blocked clients")
                .register(meterRegistry);

        Gauge.builder("mybank.accounts.arrested.count", this, MetricsConfig::getArrestedAccountsCount)
                .description("Number of arrested accounts")
                .register(meterRegistry);
    }

    public long getBlockedClientsCount() {
        return clientRepository.countByBlockedTrue();
    }

    public long getArrestedAccountsCount() {
        return accountRepository.countByStatus(Account.Status.ARRESTED);
    }

    @Scheduled(fixedRate = 60000)
    public void refreshMetrics() {
        this.blockedClientsCount = clientRepository.countByBlockedTrue();
        this.arrestedAccountsCount = accountRepository.countByStatus(Account.Status.ARRESTED);
    }
}