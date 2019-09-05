package cn.kastner.oj.repository;

import cn.kastner.oj.domain.stat.UserTagStat;
import cn.kastner.oj.repository.result.TagScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserTagStatRepository
    extends JpaRepository<UserTagStat, String>, JpaSpecificationExecutor<UserTagStat> {
  Optional<UserTagStat> findByUserIdAndTagId(String userId, String tagId);

  @Query(
      value =
          "select sum(UserTagStat.score) as score, UserTagStat.tag_id as tagId from user_tag_stat UserTagStat where user_id in ?1 group by tag_id order by score asc limit ?2",
      nativeQuery = true)
  List<TagScore> findSumOfScoreGroupByUserId(List<String> userIdList, Integer limit);
}
