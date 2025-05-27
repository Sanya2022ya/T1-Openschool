package t1project.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class KafkaListenerService {

    @KafkaListener(topics = "t1_demo_metrics", groupId = "t1_group")
    public void listen(ConsumerRecord<String, String> record) {
        String topic = record.topic();
        String key = record.key();
        String message = record.value();

        // Достаём заголовок errorType
        String errorType = "UNKNOWN";
        Header header = record.headers().lastHeader("errorType");
        if (header != null) {
            errorType = new String(header.value(), StandardCharsets.UTF_8);
        }

        // Выводим с разбивкой по типу
        log.info("📥 Получено сообщение из Kafka:");
        log.info("▶️ Топик: {}", topic);
        log.info("🔑 Ключ: {}", key);
        log.info("📄 Значение: {}", message);
        log.info("⚠️ Тип ошибки: {}", errorType);

        if ("DATA_SOURCE".equalsIgnoreCase(errorType)) {
            log.warn("🛑 Это сообщение об ошибке источника данных!");
        } else if ("METRICS".equalsIgnoreCase(errorType)) {
            log.info("⏱ Это сообщение о метриках времени выполнения.");
        } else {
            log.info("ℹ️ Неизвестный тип ошибки.");
        }
    }
}
