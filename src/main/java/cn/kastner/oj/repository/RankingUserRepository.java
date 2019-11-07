package cn.kastner.oj.repository;

import cn.kastner.oj.domain.Contest;
import cn.kastner.oj.domain.RankingUser;
import cn.kastner.oj.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RankingUserRepository
    extends JpaRepository<RankingUser, String>, JpaSpecificationExecutor<RankingUser> {
  List<RankingUser> findByContest(Contest contest);

  List<RankingUser> findByContestOrderByPassedCountDescTimeAsc(Contest contest);

  RankingUser findByContestAndUser(Contest contest, User user);

  void deleteAllByContest(Contest contest);
}
