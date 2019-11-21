package cn.kastner.oj.factory;

import cn.kastner.oj.domain.Contest;
import cn.kastner.oj.domain.ContestProblem;
import cn.kastner.oj.domain.RankingUser;
import cn.kastner.oj.domain.TimeCost;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class TimeCostFactory {

  private TimeCostFactory() {
  }

  public static List<TimeCost> createList(Contest contest, RankingUser rankingUser) {
    Set<ContestProblem> contestProblemSet = contest.getContestProblemSet();
    List<TimeCost> timeCostList = new ArrayList<>();
    for (ContestProblem contestProblem : contestProblemSet) {
      TimeCost timeCost = new TimeCost(contestProblem, rankingUser);
      timeCostList.add(timeCost);
    }
    return timeCostList;
  }
}
