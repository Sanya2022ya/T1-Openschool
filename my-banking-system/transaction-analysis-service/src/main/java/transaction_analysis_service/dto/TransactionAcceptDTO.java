package transaction_analysis_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionAcceptDTO {
    private UUID transactionId;
    private Long accountId;
    private UUID clientId;
    private double amount;
    private BigDecimal balance;
    private LocalDateTime timestamp;
}
