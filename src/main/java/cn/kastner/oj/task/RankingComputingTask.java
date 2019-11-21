package cn.kastner.oj.task;

import cn.kastner.oj.domain.Contest;
import cn.kastner.oj.domain.RankingUser;
import cn.kastner.oj.domain.TimeCost;
import cn.kastner.oj.domain.enums.ContestStatus;
import cn.kastner.oj.domain.enums.ContestType;
import cn.kastner.oj.repository.ContestRepository;
import cn.kastner.oj.repository.RankingUserRepository;
import cn.kastner.oj.repository.TimeCostRepository;
import cn.kastner.oj.util.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class RankingComputingTask {

  private final ContestRepository contestRepository;

  private final RankingUserRepository rankingUserRepository;

  private final TimeCostRepository timeCostRepository;

  private final RedisTemplate redisTemplate;

  private final DTOMapper mapper;

  @Autowired
  public RankingComputingTask(
      ContestRepository contestRepository,
      RankingUserRepository rankingUserRepository,
      TimeCostRepository timeCostRepository,
      RedisTemplate redisTemplate,
      DTOMapper mapper) {
    this.contestRepository = contestRepository;
    this.rankingUserRepository = rankingUserRepository;
    this.timeCostRepository = timeCostRepository;
    this.redisTemplate = redisTemplate;
    this.mapper = mapper;
  }

  @Scheduled(fixedRate = 10000)
  public void computeRank() {
    List<Contest> contestList = contestRepository.findByStatus(ContestStatus.PROCESSING);
    for (Contest contest : contestList) {
      List<RankingUser> rankingUserList = updateRanking(contest);
      if (contest.getContestType().equals(ContestType.ICPC)) {
        if (contest
            .getStartDate()
            .isAfter(
                LocalDateTime.now()
                    .minusMinutes(Duration.ofMillis(contest.getFrozenOffset()).toMinutes()))) {
          updateRankingCache(contest, rankingUserList);
        } else {
          if (!contest.getFrozen()) {
            contest.setFrozen(true);
            contestRepository.save(contest);
          }
        }
      } else {
        List<RankingUser> exist = (List<RankingUser>) redisTemplate.opsForValue().get("rankingUserList:" + contest.getId());
        if (exist == null) {
          updateRankingCache(contest, rankingUserList);
        }
      }
    }
  }

  private List<RankingUser> updateRanking(Contest contest) {
    List<RankingUser> rankingUserList;
    if (contest.getContestType().equals(ContestType.ICPC)) {
      rankingUserList = rankingUserRepository.findByContestOrderByPassedCountDescTimeAsc(contest);
    } else {
      rankingUserList = rankingUserRepository.findByContestOrderByScoreDescTimeAsc(contest);
    }
    for (int i = 0; i < rankingUserList.size(); i++) {
      RankingUser rankingUser = rankingUserList.get(i);
      List<TimeCost> timeCostList = timeCostRepository.findByRankingUser(rankingUser);
      rankingUser.setTimeList(timeCostList);
      rankingUser.setRankingNumber(i);
      if (!rankingUser.getRanked()) {
        i--;
      }
    }
    return rankingUserRepository.saveAll(rankingUserList);


  }

  private void updateRankingCache(Contest contest, List<RankingUser> rankingUserList) {
    for (RankingUser rankingUser : rankingUserList) {
      redisTemplate
          .opsForValue()
          .set("timeCostList:" + rankingUser.getId(), mapper.toLabelTimeCostDTOs(rankingUser.getTimeList()));
    }
    redisTemplate
        .opsForValue()
        .set(
            "rankingUserList:" + contest.getId(),
            mapper.toRankingUserDTOs(rankingUserList));
  }
}
