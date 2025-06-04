package t1project.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TransactionRequestDTO {
    private UUID clientId;
    private Long accountId;
    private Double amount;
}
