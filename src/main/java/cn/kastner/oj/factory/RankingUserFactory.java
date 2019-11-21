package cn.kastner.oj.factory;

import cn.kastner.oj.domain.Contest;
import cn.kastner.oj.domain.Group;
import cn.kastner.oj.domain.RankingUser;
import cn.kastner.oj.domain.User;

public final class RankingUserFactory {

  private RankingUserFactory() {
  }

  public static RankingUser create(User user, Contest contest, Group group) {
    RankingUser rankingUser = new RankingUser();
    rankingUser.setContest(contest);
    rankingUser.setUser(user);

    if (null != group) {
      rankingUser.setGroupId(group.getId());
      rankingUser.setTeacherId(group.getAuthor().getId());
    }

    // process exclude rankingUser
    if (!contest.getUserListExcluded().contains(user)) {
      rankingUser.setRanked(true);
    } else {
      rankingUser.setRanked(false);
    }
    return rankingUser;
  }
}
