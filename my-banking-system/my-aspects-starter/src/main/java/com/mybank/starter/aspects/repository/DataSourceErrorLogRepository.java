package com.mybank.starter.aspects.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mybank.starter.aspects.model.DataSourceErrorLog;

public interface DataSourceErrorLogRepository extends JpaRepository<DataSourceErrorLog, Long> {
}
