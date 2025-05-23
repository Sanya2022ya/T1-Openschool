package t1project.aspect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class CachedAspect {
    private final Map<String, CacheEntry> cache = new HashMap<>();

    @Value("${cache.ttl.seconds:30}")
    private long ttlSeconds;

    @Around("@annotation(t1project.aspect.Cached)")
    public Object cacheResult(ProceedingJoinPoint joinPoint) throws Throwable {
        String key = "account_" + joinPoint.getArgs()[0];

        if (cache.containsKey(key) && !isExpired(cache.get(key))) {
            System.out.println("[CACHE HIT] Данные для аккаунта " + joinPoint.getArgs()[0] + " взяты из кэша");
            return cache.get(key).getValue();
        }

        Object result = joinPoint.proceed();
        cache.put(key, new CacheEntry(result, System.currentTimeMillis()));
        return result;
    }

    private boolean isExpired(CacheEntry entry) {
        return System.currentTimeMillis() - entry.getTimestamp() > TimeUnit.SECONDS.toMillis(ttlSeconds);
    }

    @Getter
    @AllArgsConstructor
    private static class CacheEntry {
        private final Object value;
        private final long timestamp;
    }
}
