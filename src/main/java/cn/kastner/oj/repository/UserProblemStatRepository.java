package cn.kastner.oj.repository;

import cn.kastner.oj.domain.stat.UserProblemStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserProblemStatRepository extends JpaRepository<UserProblemStat, String>, JpaSpecificationExecutor<UserProblemStat> {
  Optional<UserProblemStat> findByUserIdAndProblemId(String userId, String problemId);
}
