package cn.kastner.oj.repository;

import cn.kastner.oj.domain.Contest;
import cn.kastner.oj.domain.ContestProblem;
import cn.kastner.oj.domain.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ContestProblemRepository
    extends JpaRepository<ContestProblem, String>, JpaSpecificationExecutor<ContestProblem> {
  List<ContestProblem> findByContest(Contest contest);

  ContestProblem findByContestAndProblem(Contest contest, Problem problem);

  List<ContestProblem> findByProblem(Problem problem);

  List<ContestProblem> findAllByProblemAndContest(Iterable<Problem> problems, Contest contest);

  void deleteAllByProblemAndContest(Iterable<Problem> problems, Contest contest);
}
