package t1project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String key, String message, String errorType) {
        try {
            kafkaTemplate.send(
                    MessageBuilder.withPayload(message)
                            .setHeader(KafkaHeaders.TOPIC, topic)
                            .setHeader(KafkaHeaders.KEY, key)
                            .setHeader("errorType", errorType)
                            .build()
            );
        } catch (Exception e) {
            // сюда можно записать лог в БД, если Kafka недоступна
            throw new RuntimeException("Ошибка при отправке Kafka-сообщения", e);
        }
    }
}
