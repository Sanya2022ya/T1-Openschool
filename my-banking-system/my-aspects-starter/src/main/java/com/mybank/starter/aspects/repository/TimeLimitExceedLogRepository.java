package com.mybank.starter.aspects.repository;

import com.mybank.starter.aspects.model.TimeLimitExceedLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeLimitExceedLogRepository extends JpaRepository<TimeLimitExceedLog, Long> {
}
