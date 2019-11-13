package cn.kastner.oj.factory;

import cn.kastner.oj.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class RankingUserFactory {

  private RankingUserFactory() {
  }

  public static RankingUser create(User user, Contest contest, Group group) {
    Set<ContestProblem> contestProblemSet = contest.getContestProblemSet();
    RankingUser rankingUser = new RankingUser();
    rankingUser.setContest(contest);
    rankingUser.setUser(user);

    if (null != group) {
      rankingUser.setGroupId(group.getId());
      rankingUser.setTeacherId(group.getAuthor().getId());
    }

    // initialize timeCostList
    List<TimeCost> timeCostList = new ArrayList<>();
    for (ContestProblem contestProblem : contestProblemSet) {

      TimeCost timeCost = new TimeCost();
      timeCost.setContestProblem(contestProblem);
      timeCost.setRankingUser(rankingUser);
      timeCost.setFrozen(false);
      timeCostList.add(timeCost);
    }
    rankingUser.setTimeList(timeCostList);

    // process exclude rankingUser
    if (!contest.getUserListExcluded().contains(user)) {
      rankingUser.setRanked(true);
    } else {
      rankingUser.setRanked(false);
    }
    return rankingUser;
  }
}
