package t1project.aspect;

import t1project.model.TimeLimitExceedLog;
import t1project.repository.TimeLimitExceedLogRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class MetricAspect {

    private final TimeLimitExceedLogRepository logRepository;

    @Value("${metric.time.limit.ms}")
    private long timeLimitMs;

    @Around("@annotation(t1project.aspect.Metric)")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;

        if (duration > timeLimitMs) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            TimeLimitExceedLog logEntry = new TimeLimitExceedLog();
            logEntry.setMethodName(signature.getMethod().getName());
            logEntry.setExecutionTime(duration);
            logEntry.setExceededLimit(timeLimitMs);
            logRepository.save(logEntry);
        }

        return result;
    }
}
