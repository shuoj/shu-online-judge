package cn.kastner.oj.repository;

import cn.kastner.oj.domain.ContestProblem;
import cn.kastner.oj.domain.RankingUser;
import cn.kastner.oj.domain.TimeCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TimeCostRepository
    extends JpaRepository<TimeCost, String>, JpaSpecificationExecutor<TimeCost> {

  TimeCost findByRankingUserAndContestProblem(RankingUser rankingUser, ContestProblem contestProblem);

  List<TimeCost> findByRankingUser(RankingUser rankingUser);

  void deleteAllByRankingUser(RankingUser rankingUser);

  void deleteAllByContestProblem(Iterable<ContestProblem> contestProblems);
}
