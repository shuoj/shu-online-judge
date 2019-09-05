package cn.kastner.oj.repository;

import cn.kastner.oj.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, String>, JpaSpecificationExecutor<Tag> {

  Optional<Tag> findByName(String name);

  List<Tag> findByNameContaining(String name);
}
