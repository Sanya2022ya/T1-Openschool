package com.example.transaction_common_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransactionResultDTO {
    private UUID transactionId;
    private UUID clientId;
    private Long accountId;
    private String status;
    private String reason;

}
