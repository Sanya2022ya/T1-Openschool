package transaction_analysis_service.service;

import transaction_analysis_service.dto.TransactionAcceptDTO;
import com.example.transaction_common_dto.TransactionResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionAnalysisService {

    private final KafkaProducerService producerService;

    @Value("${transaction.analysis.limit.count}")
    private int limitCount;

    @Value("${transaction.analysis.limit.seconds}")
    private int limitSeconds;

    private record ClientAccountKey(UUID clientId, Long accountId) {}

    private static class TransactionRecord {
        final UUID transactionId;
        final Instant timestamp;

        TransactionRecord(UUID transactionId, Instant timestamp) {
            this.transactionId = transactionId;
            this.timestamp = timestamp;
        }
    }

    private final Map<ClientAccountKey, List<TransactionRecord>> transactionsMap = new HashMap<>();

    @KafkaListener(topics = "t1_demo_transaction_accept", groupId = "t1_analysis_group", containerFactory = "transactionKafkaListenerContainerFactory")
    public void analyze(TransactionAcceptDTO dto) {
        log.info("🔍 Анализ транзакции: {}", dto);

        UUID clientId = dto.getClientId();
        Long accountId = dto.getAccountId();
        UUID transactionId = dto.getTransactionId();
        double amount = dto.getAmount();
        BigDecimal balance = dto.getBalance();

        if (amount < 0 ) {
            BigDecimal amountBD = BigDecimal.valueOf(amount).abs();
            if (amountBD.compareTo(balance) > 0) {
                sendResult(transactionId, clientId, accountId, "REJECTED", "Недостаточно средств");
                return;
            }
        }

        Instant transactionTime = dto.getTimestamp() != null
                ? dto.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()
                : Instant.now();

        ClientAccountKey key = new ClientAccountKey(clientId, accountId);
        transactionsMap.putIfAbsent(key, new ArrayList<>());

        List<TransactionRecord> transactionList = transactionsMap.get(key);

        transactionList.add(new TransactionRecord(transactionId, transactionTime));

        Instant windowStart = transactionTime.minusSeconds(limitSeconds);
        transactionList.removeIf(tr -> tr.timestamp.isBefore(windowStart));

        boolean blocked = transactionList.size() > limitCount;

        if (blocked) {
            for (TransactionRecord tr : transactionList) {
                sendResult(tr.transactionId, clientId, accountId, "BLOCKED", "Частые транзакции");
            }
            transactionList.clear();
        } else {
            sendResult(transactionId, clientId, accountId, "ACCEPTED", "Ок");
        }
    }


    private void sendResult(UUID transactionId, UUID clientId, Long accountId, String status, String reason) {
        TransactionResultDTO result = new TransactionResultDTO();
        result.setTransactionId(transactionId);
        log.info("transactionId = {}",transactionId);
        result.setClientId(clientId);
        result.setAccountId(accountId);
        result.setStatus(status);
        result.setReason(reason);

        producerService.sendResult(result);
        log.info("📤 Отправлен статус '{}' для транзакции {} по клиенту {}, счету {}. Причина: {}", status, transactionId, clientId, accountId, reason);
    }


}

