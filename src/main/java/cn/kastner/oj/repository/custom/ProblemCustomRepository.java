package cn.kastner.oj.repository.custom;

import cn.kastner.oj.domain.Problem;
import cn.kastner.oj.domain.Tag;
import cn.kastner.oj.domain.stat.UserProblemStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.List;

@Repository
public class ProblemCustomRepository {

  private final EntityManager em;

  @Autowired
  public ProblemCustomRepository(EntityManager em) {
    this.em = em;
  }

  List<Problem> findRandomByTagAndUserAndLastSubmitDateAndLimit(
      List<String> tagIdList, List<String> userIdList, Integer interval, Integer count) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Problem> cq = cb.createQuery(Problem.class);

    Root<Problem> problem = cq.from(Problem.class);
    ListJoin<Problem, Tag> problemTagJoin = problem.joinList("tagList");
    Subquery<UserProblemStat> subquery = cq.subquery(UserProblemStat.class);
    Root<UserProblemStat> userProblemStat = subquery.from(UserProblemStat.class);
    //    Expression<Timestamp> timeDiff = cb.function(
    //        "timestampdiff",
    //        Timestamp.class,
    //        userProblemStat.get("lastSubmitDate"),
    //        cb.currentTimestamp());
    //    Expression timestampdiff = cb.function("timestampdiff", Integer.class, "second",
    // userProblemStat.get("lastSubmitDate"), cb.currentTimestamp());
    subquery.where(
        cb.and(
            cb.in(userProblemStat.get("id")).value(userIdList),
            cb.equal(userProblemStat.get("problemId"), problem.<String>get("id"))));
    cq.where(
        cb.and(
            cb.isMember(tagIdList, problemTagJoin.get("tagList").get("id")),
            cb.not(cb.exists(subquery))));
    return em.createQuery(cq).getResultList();
  }
}
