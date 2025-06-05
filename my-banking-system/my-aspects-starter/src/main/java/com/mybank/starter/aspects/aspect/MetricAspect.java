package com.mybank.starter.aspects.aspect;

import com.mybank.starter.aspects.model.TimeLimitExceedLog;
import com.mybank.starter.aspects.repository.TimeLimitExceedLogRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.mybank.common.kafka.service.KafkaProducerService;

@Aspect
@Component
@RequiredArgsConstructor
public class MetricAspect {

    private final TimeLimitExceedLogRepository logRepository;
    private final KafkaProducerService kafkaProducerService;

    @Value("${metric.time.limit.ms}")
    private long timeLimitMs;

    @Around("@annotation(t1project.aspect.Metric)")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;

        if (duration > timeLimitMs) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String methodName = signature.getMethod().getName();

            TimeLimitExceedLog logEntry = new TimeLimitExceedLog();
            logEntry.setMethodName(methodName);
            logEntry.setExecutionTime(duration);
            logEntry.setExceededLimit(timeLimitMs);
            logRepository.save(logEntry);

            String message = "Метод " + methodName + " превысил лимит времени: " + duration + "мс";

            try {
                kafkaProducerService.sendMessage("t1_demo_metrics", "metrics", message, "METRICS");
            } catch (Exception e) {
            }
        }

        return result;
    }
}
