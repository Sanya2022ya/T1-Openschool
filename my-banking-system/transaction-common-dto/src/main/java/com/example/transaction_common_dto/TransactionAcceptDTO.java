package com.example.transaction_common_dto; // ВОТ ЭТО ОЧЕНЬ ВАЖНО ИЗМЕНИТЬ!

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; // @Getter здесь не нужен, так как @Data его уже включает

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data // Генерирует геттеры, сеттеры, equals, hashCode, toString для всех полей
@NoArgsConstructor // Генерирует конструктор без аргументов
@AllArgsConstructor // Генерирует конструктор со всеми полями
public class TransactionAcceptDTO {
    private UUID clientId;
    private Long accountId;
    private UUID transactionId;
    private LocalDateTime timestamp;
    private BigDecimal amount;     // Теперь точно BigDecimal
    private String status;         // Поле для статуса
    private BigDecimal balance;    // Поле для баланса

}