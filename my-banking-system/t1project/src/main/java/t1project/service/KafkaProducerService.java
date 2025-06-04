package t1project.service;

import com.example.transaction_common_dto.TransactionResultDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import com.example.transaction_common_dto.TransactionAcceptDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> stringKafkaTemplate;
    private final KafkaTemplate<String, Object> jsonKafkaTemplate;

    public void sendMessage(String topic, String key, String message, String errorType) {
        try {
            Message<String> msg = MessageBuilder.withPayload(message)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.KEY, key)
                    .setHeader("errorType", errorType)
                    .build();

            stringKafkaTemplate.send(msg);
            log.info("📤 Отправлено сообщение в топик {} с ключом {}", topic, key);
        } catch (Exception e) {
            log.error("❌ Ошибка при отправке сообщения в Kafka", e);
            throw new RuntimeException("Ошибка при отправке Kafka-сообщения", e);
        }
    }

    public void sendTransactionAccept(TransactionAcceptDTO dto) {
        try {
            jsonKafkaTemplate.send("t1_demo_transaction_accept", dto.getTransactionId().toString(), dto);
            log.info("📤 Отправлено сообщение в Kafka (topic: t1_demo_transaction_accept). TransactionId: {}, Статус: {}",
                    dto.getTransactionId(), dto.getStatus());
        } catch (Exception e) {
            log.error("❌ Ошибка при отправке TransactionAcceptDTO в Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при отправке TransactionAcceptDTO в Kafka", e);
        }
    }
    public void sendTransactionResult(TransactionResultDTO dto) {
        try {
            jsonKafkaTemplate.send("t1_demo_transaction_result", dto.getTransactionId().toString(), dto);
            log.info("📤 Отправлен результат транзакции в Kafka (topic: t1_demo_transaction_result). TransactionId: {}, Статус: {}",
                    dto.getTransactionId(), dto.getStatus());
        } catch (Exception e) {
            log.error("❌ Ошибка при отправке TransactionResultDTO в Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при отправке TransactionResultDTO в Kafka", e);
        }
    }
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendMessageObject(String topic, String key, Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            kafkaTemplate.send(topic, key, json);
            log.info("📤 Отправлено сообщение в Kafka. Топик: {}, Ключ: {}, Объект: {}", topic, key, json);
        } catch (JsonProcessingException e) {
            log.error("❌ Ошибка сериализации объекта для Kafka", e);
        }
    }
}