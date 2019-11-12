package cn.kastner.oj.repository;

import cn.kastner.oj.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository
    extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
  User findByUsername(String username);

  Optional<User> findUserByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  List<User> findByUsernameContaining(String username, Pageable pageable);

  Optional<User> findByEmail(String email);
}
