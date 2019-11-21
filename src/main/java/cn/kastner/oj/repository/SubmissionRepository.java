package cn.kastner.oj.repository;

import cn.kastner.oj.domain.Contest;
import cn.kastner.oj.domain.Problem;
import cn.kastner.oj.domain.Submission;
import cn.kastner.oj.domain.User;
import cn.kastner.oj.domain.enums.Result;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SubmissionRepository
    extends JpaRepository<Submission, String>, JpaSpecificationExecutor<Submission> {
  List<Submission> findByContest(Contest contest, Pageable pageable);

  Long countByContest(Contest contest);

  List<Submission> findByContestAndIsBeforeFrozen(
      Contest contest, Boolean isBeforeFrozen, Pageable pageable);

  Long countByContestAndIsBeforeFrozen(Contest contest, Boolean isBeforeFrozen);

  List<Submission> findByContestAndProblemAndIsPractice(
      Contest contest, Problem problem, Boolean isPractice);

  List<Submission> findByContestAndProblemAndIsPracticeAndAuthor(
      Contest contest, Problem problem, Boolean isPractice, User user);

  List<Submission> findByProblemAndIsPracticeAndAuthor(
      Problem problem, Boolean isPractice, User user);

  List<Submission> findByIsPractice(Boolean isPractice, Pageable pageable);

  Long countByIsPractice(Boolean isPractice);

  List<Submission> findByAuthorAndIsPractice(User author, Boolean isPractice, Pageable pageable);

  Long countByAuthorAndIsPractice(User author, Boolean isPractice);

  List<Submission> findByContestAndProblemAndIsPracticeAndAuthorAndResult(
      Contest contest, Problem problem, Boolean isPractice, User author, Result result);

  void deleteAllByContest(Contest contest);
}
