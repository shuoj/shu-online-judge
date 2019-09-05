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
    List<TimeCost> timeCostList = new ArrayList<>();
    for (ContestProblem contestProblem : contestProblemSet) {
      TimeCost timeCost = new TimeCost();
      timeCost.setContestProblem(contestProblem);
      timeCost.setRankingUser(rankingUser);
      timeCost.setFrozen(false);
      timeCostList.add(timeCost);
    }
    rankingUser.setTimeListAfter(timeCostList);
    for (TimeCost tc : timeCostList) {
      tc.setFrozen(true);
    }
    rankingUser.setTimeListBefore(timeCostList);

    TimeCost timeCost = new TimeCost();
    rankingUser.setTotalTimeAfter(timeCost);
    rankingUser.setTotalTimeBefore(timeCost);

    // process exclude rankingUser
    if (!ranking.getUserListExcluded().contains(user)) {
      rankingUser.setRanked(true);
    } else {
      rankingUser.setRanked(false);
    }
    return rankingUser;
  }
}
