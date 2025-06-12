package com.example.transaction_common_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnarrestAccountResponseDTO {
    private UUID accountId;
    private boolean unarrested;
    private String reason;
}