package cn.kastner.oj.repository.custom;

import cn.kastner.oj.domain.User;
import cn.kastner.oj.domain.log.AuthLog;
import cn.kastner.oj.dto.AuthLogDTO;
import cn.kastner.oj.query.AuthLogQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AuthLogCustomRepository {

  private final EntityManager em;

  @Autowired
  public AuthLogCustomRepository(EntityManager entityManager) {
    this.em = entityManager;
  }

  List<AuthLogDTO> findByUsername(AuthLogQuery query, Pageable pageable) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<AuthLogDTO> cq = cb.createQuery(AuthLogDTO.class);

    Root<AuthLog> authLog = cq.from(AuthLog.class);
    List<Predicate> predicateList = new ArrayList<>();
    String username = query.getUsername();
    if (username != null) {
      Join<AuthLog, User> authLogUserJoin = authLog.join("user", JoinType.LEFT);
      predicateList.add(
          cb.like(authLogUserJoin.get("username").as(String.class), "%" + username + "%"));
    }
    Predicate[] p = new Predicate[predicateList.size()];
    cq.where(predicateList.toArray(p));
    return em.createQuery(cq).getResultList();
  }
}
