package t1project.dto;

import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionAcceptDTO {
    private UUID clientId;
    private Long accountId;
    private UUID transactionId;
    private LocalDateTime timestamp;
    private Double amount;
    @Getter
    private BigDecimal balance;

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

}
