package cn.kastner.oj.repository;

import cn.kastner.oj.domain.IndexSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IndexSequenceRepository extends JpaRepository<IndexSequence, String>, JpaSpecificationExecutor {
  IndexSequence findByName(String name);
}
