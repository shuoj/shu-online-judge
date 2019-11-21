package cn.kastner.oj.aspect;

import cn.kastner.oj.domain.*;
import cn.kastner.oj.domain.enums.Result;
import cn.kastner.oj.domain.security.UserContext;
import cn.kastner.oj.dto.SubmissionDTO;
import cn.kastner.oj.exception.AppException;
import cn.kastner.oj.exception.ContestException;
import cn.kastner.oj.exception.ProblemException;
import cn.kastner.oj.kafka.Producer;
import cn.kastner.oj.repository.SubmissionRepository;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
public class SubmissionAspect {

  private final SubmissionRepository submissionRepository;
  private final String topic;
  private final Producer producer;

  @Autowired
  public SubmissionAspect(SubmissionRepository submissionRepository, Producer producer, @Value("${kafka.topic.submission}") String topic) {
    this.submissionRepository = submissionRepository;
    this.producer = producer;
    this.topic = topic;
  }

  @Before(
      value =
          "execution(* cn.kastner.oj.service.SubmissionService.findByContestProblem(..)) && args(contest, problem)",
      argNames = "contest,problem")
  private void requirePassProblem(Contest contest, Problem problem) throws AppException {
    if (!contest.getCouldShare()) {
      throw new ContestException(ContestException.CANNOT_SHARING);
    }

    User user = UserContext.getCurrentUser();
    List<Submission> submissionList =
        submissionRepository.findByContestAndProblemAndIsPracticeAndAuthorAndResult(
            contest, problem, false, user, Result.ACCEPTED);
    if (submissionList.isEmpty()) {
      throw new ProblemException(ProblemException.DID_NOT_PASS);
    }
  }

  @AfterReturning(
      value =
          "execution(* cn.kastner.oj.service.SubmissionService.create*(..))",
      returning = "returning",
      argNames = "returning")
  private void submissionCounter(SubmissionDTO returning) {
    producer.send(topic, "submission", returning);
  }
}
