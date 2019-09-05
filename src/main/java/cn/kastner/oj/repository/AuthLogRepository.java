package cn.kastner.oj.repository;

import cn.kastner.oj.domain.log.AuthLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuthLogRepository extends JpaRepository<AuthLog, String>, JpaSpecificationExecutor<AuthLog> {

}
