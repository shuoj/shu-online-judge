package cn.kastner.oj.repository.custom;

import cn.kastner.oj.repository.result.TagScore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserTagStatCustomRepository {

  private final EntityManager em;

  @Autowired
  public UserTagStatCustomRepository(EntityManager em) {
    this.em = em;
  }

  List<TagScore> findSumOfScoreGroupByUserId(List<String> userIdList, Integer limit) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<TagScore> cq = cb.createQuery(TagScore.class);

    Root<TagScore> tagScore = cq.from(TagScore.class);
    List<Predicate> predicateList = new ArrayList<>();
    predicateList.add(cb.in(tagScore.get("userId")).value(userIdList));


    Predicate[] p = new Predicate[predicateList.size()];
    cq.where(predicateList.toArray(p));
    return em.createQuery(cq).getResultList();
  }
}
