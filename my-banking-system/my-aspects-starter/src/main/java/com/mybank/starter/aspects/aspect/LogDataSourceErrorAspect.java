package com.mybank.starter.aspects.aspect;

import com.mybank.starter.aspects.model.DataSourceErrorLog;
import com.mybank.starter.aspects.repository.DataSourceErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import com.mybank.common.kafka.service.KafkaProducerService;
import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LogDataSourceErrorAspect {

    private final DataSourceErrorLogRepository errorLogRepository;
    private final KafkaProducerService kafkaProducerService;

    @Around("execution(* t1project.service.*.*(..))")
    public Object logDatabaseExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (RuntimeException ex) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String methodSignature = signature.toShortString();

            DataSourceErrorLog logEntry = new DataSourceErrorLog();
            logEntry.setMessage(ex.getMessage());
            logEntry.setMethodSignature(methodSignature);
            logEntry.setStackTrace(getStackTraceAsString(ex));
            errorLogRepository.save(logEntry);

            log.error("Исключение сохранено в DataSourceErrorLog: {}", ex.getMessage());

            try {
                String message = "Ошибка в методе " + methodSignature + ": " + ex.getMessage();
                kafkaProducerService.sendMessage("t1_demo_metrics", "data_source", message, "DATA_SOURCE");
            } catch (Exception kafkaException) {
                log.error("Kafka недоступна, не удалось отправить DATA_SOURCE: {}", kafkaException.getMessage());
            }

            throw ex;
        }
    }

    private String getStackTraceAsString(Throwable throwable) {
        return Arrays.stream(throwable.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
    }
}
