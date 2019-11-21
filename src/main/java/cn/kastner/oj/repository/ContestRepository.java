package cn.kastner.oj.repository;

import cn.kastner.oj.domain.Contest;
import cn.kastner.oj.domain.enums.ContestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ContestRepository
    extends JpaRepository<Contest, String>, JpaSpecificationExecutor<Contest> {
  Optional<Contest> findByName(String name);

  boolean existsByName(String name);

  List<Contest> findByStatus(ContestStatus status);
}
