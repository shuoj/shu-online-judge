package cn.kastner.oj.repository;

import cn.kastner.oj.domain.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProblemRepository
    extends JpaRepository<Problem, String>, JpaSpecificationExecutor<Problem> {

  Problem findByTitle(String title);

  @Query(
      value =
          "select count(problem.id) "
              + "from problem "
              + "       right join problem_tag on problem.id = problem_tag.problem_id "
              + "where problem_tag.tag_id in ?1 "
              + "  and not exists(select user_problem_stat.id "
              + "                 from user_problem_stat "
              + "                 where user_problem_stat.problem_id = problem.id "
              + "                   and user_problem_stat.user_id in ?2 "
              + "                   and timestampdiff(day, now(), user_problem_stat.last_submit_date) < ?3)",
      nativeQuery = true)
  Long countByTagAndUserAndLastSubmitDate(
      List<String> tagIdList, List<String> userIdList, Integer interval);

  @Query(
      value =
          "select * "
              + "from problem "
              + "       right join problem_tag on problem.id = problem_tag.problem_id "
              + "where problem_tag.tag_id in ?1 "
              + "  and not exists(select user_problem_stat.id "
              + "                 from user_problem_stat "
              + "                 where user_problem_stat.user_id in ?2 "
              + "                   and user_problem_stat.problem_id = problem.id "
              + "                   and timestampdiff(day, user_problem_stat.last_submit_date, now()) < ?3)",
      nativeQuery = true)
  List<Problem> findAllByTagAndUserAndLastSubmitDate(
      List<String> tagIdList, List<String> userIdList, Integer interval);

  @Query(
      value =
          "select * "
              + "from problem "
              + "       right join problem_tag on problem.id = problem_tag.problem_id "
              + "where problem_tag.tag_id in ?1 "
              + "  and not exists(select user_problem_stat.id "
              + "                 from user_problem_stat "
              + "                 where user_problem_stat.user_id in ?2 "
              + "                   and user_problem_stat.problem_id = problem.id "
              + "                   and timestampdiff(day, user_problem_stat.last_submit_date, now()) < ?3) "
              + "order by rand() limit ?4",
      nativeQuery = true)
  List<Problem> findRandomByTagAndUserAndLastSubmitDateAndLimit(
      List<String> tagIdList, List<String> userIdList, Integer interval, Integer count);
}
