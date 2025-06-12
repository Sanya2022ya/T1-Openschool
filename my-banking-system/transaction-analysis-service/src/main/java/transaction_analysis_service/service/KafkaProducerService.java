package transaction_analysis_service.service;

import com.example.transaction_common_dto.TransactionResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, TransactionResultDTO> kafkaTemplate;

    public void sendResult(TransactionResultDTO result) {
        kafkaTemplate.send("t1_demo_transaction_result", result.getClientId().toString(), result);
        log.info("📤 Отправлен анализ по клиенту {}", result.getClientId());
    }
}
