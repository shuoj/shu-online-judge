package cn.kastner.oj.repository;

import cn.kastner.oj.domain.User;
import cn.kastner.oj.domain.UserSecurityQuestion;
import cn.kastner.oj.domain.security.SecurityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface UserSecurityQuestionRepository
    extends JpaRepository<UserSecurityQuestion, String>,
        JpaSpecificationExecutor<UserSecurityQuestion> {
  UserSecurityQuestion findByUserAndSecurityQuestion(User user, SecurityQuestion securityQuestion);

  List<UserSecurityQuestion> findByUser(User user);
}
