package com.example.transaction_common_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientAccountCheckResponseDTO {
    public enum ClientStatus {
        UNKNOWN, BLOCKED, ACTIVE
    }
    private ClientStatus status;
    private String message;
}