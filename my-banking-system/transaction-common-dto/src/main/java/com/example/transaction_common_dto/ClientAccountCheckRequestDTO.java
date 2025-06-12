package com.example.transaction_common_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientAccountCheckRequestDTO {
    private UUID clientId;
    private Long accountId;
}