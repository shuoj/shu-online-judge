package cn.kastner.oj.repository;

import cn.kastner.oj.domain.Contest;
import cn.kastner.oj.domain.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RankingRepository
    extends JpaRepository<Ranking, String>, JpaSpecificationExecutor<Ranking> {
  Ranking findByContest(Contest contest);
}
