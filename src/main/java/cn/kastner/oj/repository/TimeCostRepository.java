package cn.kastner.oj.repository;

import cn.kastner.oj.domain.ContestProblem;
import cn.kastner.oj.domain.RankingUser;
import cn.kastner.oj.domain.TimeCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TimeCostRepository
    extends JpaRepository<TimeCost, String>, JpaSpecificationExecutor<TimeCost> {
  TimeCost findByRankingUserAndContestProblemAndFrozen(
      RankingUser rankingUser, ContestProblem contestProblem, Boolean frozen);

  TimeCost findByRankingUserAndContestProblemIsNullAndFrozen(
      RankingUser rankingUser, Boolean frozen);

  List<TimeCost> findByRankingUserAndFrozen(RankingUser rankingUser, Boolean frozen);

  List<TimeCost> findByRankingUser(RankingUser rankingUser);

  void deleteByRankingUser(RankingUser rankingUser);
}
