package cn.kastner.oj.repository;

import cn.kastner.oj.domain.Ranking;
import cn.kastner.oj.domain.RankingUser;
import cn.kastner.oj.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RankingUserRepository
    extends JpaRepository<RankingUser, String>, JpaSpecificationExecutor<RankingUser> {
  List<RankingUser> findByRanking(Ranking ranking);

  RankingUser findByRankingAndUser(Ranking ranking, User user);
}
