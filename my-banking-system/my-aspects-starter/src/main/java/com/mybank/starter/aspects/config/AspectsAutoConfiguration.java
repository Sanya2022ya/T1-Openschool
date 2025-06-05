package com.mybank.starter.aspects.config;

import com.mybank.starter.aspects.aspect.CachedAspect;
import com.mybank.starter.aspects.aspect.LogDataSourceErrorAspect;
import com.mybank.starter.aspects.aspect.MetricAspect;
import com.mybank.starter.aspects.repository.DataSourceErrorLogRepository;
import com.mybank.starter.aspects.repository.TimeLimitExceedLogRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import com.mybank.common.kafka.service.KafkaProducerService;

@Configuration
@EnableAspectJAutoProxy
@EnableJpaRepositories(basePackages = "com.mybank.starter.aspects.repository") // Включаем JPA репозитории стартера
@EntityScan(basePackages = "com.mybank.starter.aspects.model")
public class AspectsAutoConfiguration {

    // MetricAspect требует KafkaProducerService и TimeLimitExceedLogRepository
    @Bean
    @ConditionalOnMissingBean
    public MetricAspect metricAspect(TimeLimitExceedLogRepository timeLimitExceedLogRepository, KafkaProducerService kafkaProducerService) {
        return new MetricAspect(timeLimitExceedLogRepository, kafkaProducerService);
    }

    // CachedAspect не требует внешних зависимостей, кроме инжектирования @Value
    @Bean
    @ConditionalOnMissingBean
    public CachedAspect cachedAspect() {
        return new CachedAspect();
    }

    // LogDataSourceErrorAspect требует KafkaProducerService и DataSourceErrorLogRepository
    @Bean
    @ConditionalOnMissingBean
    public LogDataSourceErrorAspect logDataSourceErrorAspect(DataSourceErrorLogRepository dataSourceErrorLogRepository, KafkaProducerService kafkaProducerService) {
        return new LogDataSourceErrorAspect(dataSourceErrorLogRepository, kafkaProducerService);
    }
}