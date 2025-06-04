package transaction_analysis_service.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "transaction-analysis")
@Getter
@Setter
public class TransactionAnalysisProperties {
    private int windowSeconds;
    private int maxTransactions;
}
