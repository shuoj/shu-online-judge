package cn.kastner.oj.repository;

import cn.kastner.oj.domain.security.SecurityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SecurityQuestionRepository
    extends JpaRepository<SecurityQuestion, String>, JpaSpecificationExecutor<SecurityQuestion> {
}
