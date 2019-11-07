package cn.kastner.oj.task;

import cn.kastner.oj.domain.Contest;
import cn.kastner.oj.domain.RankingUser;
import cn.kastner.oj.domain.TimeCost;
import cn.kastner.oj.domain.enums.ContestStatus;
import cn.kastner.oj.repository.ContestRepository;
import cn.kastner.oj.repository.RankingUserRepository;
import cn.kastner.oj.repository.TimeCostRepository;
import cn.kastner.oj.util.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
  public RankingComputingTask(ContestRepository contestRepository, RankingUserRepository rankingUserRepository, TimeCostRepository timeCostRepository, RedisTemplate redisTemplate, DTOMapper mapper) {
    this.contestRepository = contestRepository;
    this.rankingUserRepository = rankingUserRepository;
    this.timeCostRepository = timeCostRepository;
    this.redisTemplate = redisTemplate;
    this.mapper = mapper;
  }

  @Scheduled(fixedRate = 10000)
  public void computeRank() {
    List<Contest> contestList =
        contestRepository.findByStatusAndStartDateAfter(ContestStatus.PROCESSING, LocalDateTime.now().minusHours(4));
    for (Contest contest : contestList) {
      List<RankingUser> rankingUserList =
          rankingUserRepository.findByContestOrderByPassedCountDescTimeAsc(contest);
      for (int i = 0; i < rankingUserList.size(); i++) {
        RankingUser rankingUser = rankingUserList.get(i);
        List<TimeCost> timeCostList = timeCostRepository.findByRankingUser(rankingUser);
        redisTemplate.opsForValue().set("timeCostList:" + rankingUser.getId(), mapper.toTimeCostDTOs(timeCostList));
        rankingUser.setRankingNumber(i);
        if (!rankingUser.getRanked()) {
          i--;
        }
      }
      redisTemplate.opsForValue().set("rankingUserList:" + contest.getId(), mapper.toRankingUserDTOs(rankingUserList));
    }
  }
}
