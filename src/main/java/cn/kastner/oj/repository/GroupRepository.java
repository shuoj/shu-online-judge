package cn.kastner.oj.repository;

import cn.kastner.oj.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GroupRepository
    extends JpaRepository<Group, String>, JpaSpecificationExecutor<Group> {
  Optional<Group> findByName(String name);
}
