package cn.kastner.oj.repository;

import cn.kastner.oj.domain.security.Authority;
import cn.kastner.oj.domain.security.AuthorityName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuthorityRepository
    extends JpaRepository<Authority, String>, JpaSpecificationExecutor<Authority> {
  Authority findByName(AuthorityName authorityName);
}
