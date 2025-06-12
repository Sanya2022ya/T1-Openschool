package com.example.transaction_common_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnlockClientResponseDTO {
    private UUID clientId;
    private boolean unlocked;
    private String reason;
}