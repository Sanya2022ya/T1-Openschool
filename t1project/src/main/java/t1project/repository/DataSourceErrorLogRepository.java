package t1project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t1project.model.DataSourceErrorLog;

public interface DataSourceErrorLogRepository extends JpaRepository<DataSourceErrorLog, Long> {
}
