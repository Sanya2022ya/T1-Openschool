package transaction_analysis_service.controller;

import com.example.transaction_common_dto.ClientAccountCheckRequestDTO;
import com.example.transaction_common_dto.ClientAccountCheckResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/api/client-check")
@RequiredArgsConstructor
@Slf4j
public class ClientCheckController {


    private final Random random = new Random();

    @PostMapping
    public ResponseEntity<ClientAccountCheckResponseDTO> checkClient(@RequestBody ClientAccountCheckRequestDTO request) {
        log.info("Received client check request for ClientId: {}, AccountId: {}", request.getClientId(), request.getAccountId());


        if (random.nextInt(100) < 20) {
            log.info("Client {} is on blacklist (simulated)", request.getClientId());
            return ResponseEntity.ok(new ClientAccountCheckResponseDTO(ClientAccountCheckResponseDTO.ClientStatus.BLOCKED, "Client is on blacklist."));
        } else {
            log.info("Client {} is active (simulated)", request.getClientId());
            return ResponseEntity.ok(new ClientAccountCheckResponseDTO(ClientAccountCheckResponseDTO.ClientStatus.ACTIVE, "Client is active."));
        }
    }
}