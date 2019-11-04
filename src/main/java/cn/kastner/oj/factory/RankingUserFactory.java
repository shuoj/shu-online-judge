package cn.kastner.oj.factory;

import cn.kastner.oj.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class RankingUserFactory {

  private RankingUserFactory() {
  }

  public static RankingUser create(User user, Contest contest) {
    Ranking ranking = contest.getRanking();
    Set<ContestProblem> contestProblemSet = contest.getContestProblemSet();
    RankingUser rankingUser = new RankingUser();
    rankingUser.setRanking(ranking);
    rankingUser.setUser(user);

    // initialize timeCostList
    List<TimeCost> timeCostListAfter = new ArrayList<>();
    List<TimeCost> timeCostListBefore = new ArrayList<>();
    for (ContestProblem contestProblem : contestProblemSet) {
      TimeCost timeCostBefore = new TimeCost();
      timeCostBefore.setContestProblem(contestProblem);
      timeCostBefore.setRankingUser(rankingUser);
      timeCostBefore.setFrozen(true);
      timeCostListBefore.add(timeCostBefore);

      TimeCost timeCostAfter = new TimeCost();
      timeCostAfter.setContestProblem(contestProblem);
      timeCostAfter.setRankingUser(rankingUser);
      timeCostAfter.setFrozen(false);
      timeCostListAfter.add(timeCostAfter);
    }
    rankingUser.setTimeListAfter(timeCostListAfter);
    rankingUser.setTimeListBefore(timeCostListBefore);

    TimeCost timeCostTotalAfter = new TimeCost();
    timeCostTotalAfter.setRankingUser(rankingUser);
    timeCostTotalAfter.setFrozen(false);
    rankingUser.setTotalTimeAfter(timeCostTotalAfter);

    TimeCost timeCostTotalBefore = new TimeCost();
    timeCostTotalBefore.setRankingUser(rankingUser);
    timeCostTotalAfter.setFrozen(true);
    rankingUser.setTotalTimeBefore(timeCostTotalBefore);


    // process exclude rankingUser
    if (!ranking.getUserListExcluded().contains(user)) {
      rankingUser.setRanked(true);
    } else {
      rankingUser.setRanked(false);
    }
    return rankingUser;
  }
}
