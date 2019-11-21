package cn.kastner.oj.service.impl;

import cn.kastner.oj.constant.CommonConstant;
import cn.kastner.oj.constant.LanguageConfig;
import cn.kastner.oj.domain.*;
import cn.kastner.oj.domain.enums.*;
import cn.kastner.oj.domain.pojos.JudgeResponse;
import cn.kastner.oj.domain.pojos.JudgeResult;
import cn.kastner.oj.domain.security.UserContext;
import cn.kastner.oj.domain.stat.UserProblemStat;
import cn.kastner.oj.domain.stat.UserTagStat;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.dto.SubmissionDTO;
import cn.kastner.oj.exception.*;
import cn.kastner.oj.query.SubmissionQuery;
import cn.kastner.oj.repository.*;
import cn.kastner.oj.service.SubmissionService;
import cn.kastner.oj.util.CommonUtil;
import cn.kastner.oj.util.DTOMapper;
import cn.kastner.oj.util.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SubmissionServiceImpl implements SubmissionService {

  private static final String COMPILE_ERROR = "CompileError";
  private static final String JUDGE_CLIENT_ERROR = "JudgeClientError";
  private final UserRepository userRepository;
  private final SubmissionRepository submissionRepository;
  private final ProblemRepository problemRepository;
  private final ContestProblemRepository contestProblemRepository;
  private final RankingUserRepository rankingUserRepository;
  private final TimeCostRepository timeCostRepository;
  private final ContestRepository contestRepository;
  private final UserProblemStatRepository userProblemStatRepository;
  private final UserTagStatRepository userTagStatRepository;
  private final DTOMapper mapper;
  private final HttpUtil http;
  private final String judgeURL;
  private final String compileURL;

  @Autowired
  public SubmissionServiceImpl(
      UserRepository userRepository,
      @Value("${judger.url}") String judgeServerBaseURL,
      SubmissionRepository submissionRepository,
      ProblemRepository problemRepository,
      ContestProblemRepository contestProblemRepository,
      RankingUserRepository rankingUserRepository,
      TimeCostRepository timeCostRepository,
      ContestRepository contestRepository,
      UserProblemStatRepository userProblemStatRepository,
      UserTagStatRepository userTagStatRepository,
      DTOMapper mapper,
      HttpUtil http) {
    this.userRepository = userRepository;
    this.judgeURL = judgeServerBaseURL + "/judge";
    this.compileURL = judgeServerBaseURL + "/compile_spj";
    this.submissionRepository = submissionRepository;
    this.problemRepository = problemRepository;
    this.contestProblemRepository = contestProblemRepository;
    this.rankingUserRepository = rankingUserRepository;
    this.timeCostRepository = timeCostRepository;
    this.contestRepository = contestRepository;
    this.userProblemStatRepository = userProblemStatRepository;
    this.userTagStatRepository = userTagStatRepository;
    this.mapper = mapper;
    this.http = http;
  }

  @Override
  public SubmissionDTO findById(String id) throws SubmissionException, ContestException {
    User user = UserContext.getCurrentUser();
    Submission submission =
        submissionRepository
            .findById(id)
            .orElseThrow(() -> new SubmissionException(SubmissionException.NO_SUCH_SUBMISSION));
    Contest contest = submission.getContest();
    if (!submission.getAuthorId().equals(user.getId())
        && !CommonUtil.isAdmin(user)
        && contest != null
        && !submission.getIsPractice()) {
      requireContestUser(contest, rankingUserRepository.findByContestAndUser(contest, user));
      requireBeforeFrozen(contest, submission);
    }
    return mapper.entityToDTO(submission);
  }

  @Override
  public PageDTO<SubmissionDTO> findByContest(String id, Integer page, Integer size)
      throws ContestException {
    Contest contest =
        contestRepository
            .findById(id)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));
    User user = UserContext.getCurrentUser();

    if (!CommonUtil.isAdmin(user)) {
      requireContestUser(contest, rankingUserRepository.findByContestAndUser(contest, user));
    }

    List<Submission> submissionList;
    Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, CommonConstant.CREATE_DATE);
    if (LocalDateTime.now().isAfter(contest.getEndDate())) {
      submissionList = submissionRepository.findByContest(contest, pageable);
      long total = submissionRepository.countByContest(contest);
      return new PageDTO<>(page, size, total, mapper.toSubmissionDTOs(submissionList));
    } else {
      submissionList = submissionRepository.findByContestAndIsBeforeFrozen(contest, true, pageable);
      long total = submissionRepository.countByContestAndIsBeforeFrozen(contest, true);
      return new PageDTO<>(page, size, total, mapper.toSubmissionDTOs(submissionList));
    }
  }

  @Override
  public PageDTO<SubmissionDTO> findByUser(Integer page, Integer size, Boolean isPractice) {
    User user = UserContext.getCurrentUser();
    Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, CommonConstant.CREATE_DATE);

    List<Submission> submissionList =
        submissionRepository.findByAuthorAndIsPractice(user, isPractice, pageable);
    long total = submissionRepository.countByAuthorAndIsPractice(user, isPractice);
    return new PageDTO<>(page, size, total, mapper.toSubmissionDTOs(submissionList));
  }

  @Override
  public List<SubmissionDTO> findByPracticeProblem(String problemId) throws ProblemException {
    Problem problem =
        problemRepository
            .findById(problemId)
            .orElseThrow(() -> new ProblemException(ProblemException.NO_SUCH_PROBLEM));
    User user = UserContext.getCurrentUser();
    List<Submission> submissionList =
        submissionRepository.findByProblemAndIsPracticeAndAuthor(problem, true, user);
    return mapper.toSubmissionDTOs(submissionList);
  }

  @Override
  public List<SubmissionDTO> findByContestProblem(String contestId, String problemId)
      throws ProblemException, ContestException {
    Problem problem =
        problemRepository
            .findById(problemId)
            .orElseThrow(() -> new ProblemException(ProblemException.NO_SUCH_PROBLEM));
    Contest contest =
        contestRepository
            .findById(contestId)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));
    User user = UserContext.getCurrentUser();
    List<Submission> submissionList =
        submissionRepository.findByContestAndProblemAndIsPracticeAndAuthor(
            contest, problem, false, user);
    return mapper.toSubmissionDTOs(submissionList);
  }

  public SubmissionDTO createContestSubmission(SubmissionDTO submissionDTO)
      throws ProblemException, ContestException, SubmissionException {
    User user = UserContext.getCurrentUser();
    Submission submission = mapper.dtoToEntity(submissionDTO);
    submission.setAuthor(user);

    Contest contest =
        contestRepository
            .findById(submissionDTO.getContestId())
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));
    boolean isICPC = contest.getContestType().equals(ContestType.ICPC);
    Optional<RankingUser> rankingUserOptional = rankingUserRepository.findByContestAndUser(contest, user);
    if (!user.isAdmin()) {
      requireContestUser(contest, rankingUserOptional);
      requireContestOnGoing(contest);
    }

    Problem problem =
        problemRepository
            .findById(submissionDTO.getProblemId())
            .orElseThrow(() -> new ProblemException(ProblemException.NO_SUCH_PROBLEM));

    submission.setContest(contest);
    submission.setIsPractice(false);
    submission.setProblem(problem);

    long duration = Duration.between(contest.getStartDate(), LocalDateTime.now()).toMillis();

    JudgeResult judgeResult = judge(submission, problem);

    submission.setDuration(judgeResult.getRealTime());
    submission.setResult(judgeResult.getResult());
    submission.setResultDetail(JSON.toJSONString(judgeResult));
    submission.setMemory(judgeResult.getMemory());
    submissionRepository.save(submission);

    if (rankingUserOptional.isPresent()) {
      ContestProblem contestProblem =
          contestProblemRepository.findByContestAndProblem(contest, problem);
      RankingUser rankingUser = rankingUserOptional.get();

      TimeCost timeCost =
          timeCostRepository.findByRankingUserAndContestProblem(rankingUser, contestProblem);

      double score = 0.0;

      if (!isICPC) {
        score =
            contestProblem.getScore()
                * (double) judgeResult.getPassedCount()
                / judgeResult.getTotalCount();
      }

      // if problem has been passed, the submission won't produce any effects on ranking
      if ((isICPC && !timeCost.getPassed()) || (!isICPC && score > timeCost.getScore())) {

        rankingUser.addTime(duration);
        rankingUser.increaseSubmitCount();
        if (!isICPC) {
          rankingUser.addScore(score - timeCost.getScore());
        }

        contestProblem.increaseSubmitCount();

        timeCost.setTotalTime(duration);
        timeCost.setSubmitted(true);
        if (!isICPC) {
          timeCost.setScore(score);
        }

        if (submission.getResult() == Result.ACCEPTED) {
          rankingUser.increaseAcceptCount();
          contestProblem.increaseAcceptCount();
          if (contestProblem.getFirstSubmission() == null) {
            contestProblem.setFirstSubmission(submission);
            timeCost.setFirstPassed(true);
          }
          if (!timeCost.getPassed()) {
            timeCost.setPassed(true);
          }
        } else {
          if (!timeCost.getPassed()) {
            timeCost.increaseErrorCount();
            rankingUser.addTime(Duration.ofMinutes(30).toMillis());
          }
        }

        contestProblem.computeAcceptRate();
        timeCostRepository.save(timeCost);
        rankingUserRepository.save(rankingUser);
        contestProblemRepository.save(contestProblem);
      }
    }

    return mapper.entityToDTO(submission);
  }


  @Override
  public SubmissionDTO createPracticeSubmission(SubmissionDTO submissionDTO)
      throws ProblemException, SubmissionException {
    User user = UserContext.getCurrentUser();
    Optional<Problem> problemOptional = problemRepository.findById(submissionDTO.getProblemId());
    if (!problemOptional.isPresent()) {
      throw new ProblemException(ProblemException.NO_SUCH_PROBLEM);
    }
    Problem problem = problemOptional.get();

    Submission submission = mapper.dtoToEntity(submissionDTO);
    submission.setAuthor(user);
    submission.setContest(null);
    submission.setProblem(problem);
    submission.setIsPractice(true);

    JudgeResult judgeResult = judge(submission, problem);

    submission.setDuration(judgeResult.getRealTime());
    submission.setResult(judgeResult.getResult());
    submission.setResultDetail(JSON.toJSONString(judgeResult));
    submission.setMemory(judgeResult.getMemory());
    return mapper.entityToDTO(submissionRepository.save(submission));
  }

  @Override
  public SubmissionDTO rejudgeSubmission(String id) throws SubmissionException {
    User user = UserContext.getCurrentUser();
    Submission submission =
        submissionRepository
            .findById(id)
            .orElseThrow(() -> new SubmissionException(SubmissionException.NO_SUCH_SUBMISSION));
    User author = submission.getAuthor();
    if (!author.getId().equals(user.getId()) && !CommonUtil.isAdmin(user)) {
      throw new SubmissionException(SubmissionException.NOT_AUTHOR);
    }
    Problem problem = submission.getProblem();
    Contest contest = submission.getContest();
    ContestProblem contestProblem =
        contestProblemRepository.findByContestAndProblem(contest, problem);

    JudgeResult judgeResult = judge(submission, problem);

    submission.setDuration(judgeResult.getRealTime());
    submission.setResult(judgeResult.getResult());
    submission.setResultDetail(JSON.toJSONString(judgeResult));
    submission.setMemory(judgeResult.getMemory());
    if (contestProblem.getFirstSubmission() == null) {
      contestProblem.setFirstSubmission(submission);
    } else {
      Submission firstSubmission = contestProblem.getFirstSubmission();
      if (!firstSubmission.getCreateDate().isBefore(submission.getCreateDate())) {
        contestProblem.setFirstSubmission(submission);
      }
    }
    return mapper.entityToDTO(submissionRepository.save(submission));
  }

  @Override
  public PageDTO<SubmissionDTO> findAll(Integer page, Integer size, SubmissionQuery submissionQuery)
      throws ProblemException, UserException {
    Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, CommonConstant.CREATE_DATE);

    String problemId = submissionQuery.getProblemId();
    Problem problem = null;
    if (null != problemId) {
      problem =
          problemRepository
              .findById(problemId)
              .orElseThrow(() -> new ProblemException(ProblemException.NO_SUCH_PROBLEM));
    }

    String username = submissionQuery.getUsername();
    User user = null;
    if (null != username) {
      user =
          userRepository
              .findUserByUsername(username)
              .orElseThrow(() -> new UserException(UserException.NO_SUCH_USER));
    }

    Problem finalProblem = problem;
    User finalUser = user;
    Specification<Submission> specification =
        (root, criteriaQuery, criteriaBuilder) -> {
          List<Predicate> predicateList = new ArrayList<>();

          if (null != finalProblem) {
            predicateList.add(criteriaBuilder.equal(root.get("problem"), finalProblem));
          }

          if (null != finalUser) {
            predicateList.add(criteriaBuilder.equal(root.get("author"), finalUser));
          }

          Language language = submissionQuery.getLanguage();
          if (null != language) {
            predicateList.add(criteriaBuilder.equal(root.get("language"), language));
          }

          Boolean isPractice = submissionQuery.getIsPractice();
          if (null != isPractice) {
            predicateList.add(criteriaBuilder.equal(root.get("isPractice"), isPractice));
          }

          Predicate[] p = new Predicate[predicateList.size()];
          return criteriaBuilder.and(predicateList.toArray(p));
        };
    List<Submission> submissionList =
        submissionRepository.findAll(specification, pageable).getContent();

    List<SubmissionDTO> submissionDTOList = mapper.toSubmissionDTOs(submissionList);
    long count = submissionRepository.count(specification);
    return new PageDTO<>(page, size, count, submissionDTOList);
  }

  @Override
  public void counter(SubmissionDTO submissionDTO) throws ProblemException, UserException {
    User user =
        userRepository
            .findById(submissionDTO.getAuthorId())
            .orElseThrow(() -> new UserException(UserException.NO_SUCH_USER));
    Problem problem =
        problemRepository
            .findById(submissionDTO.getProblemId())
            .orElseThrow(() -> new ProblemException(ProblemException.NO_SUCH_PROBLEM));

    userStatCounter(submissionDTO, user);
    problemStatCounter(submissionDTO, problem);
    userProblemStatCounter(submissionDTO, user, problem);
  }

  private void userStatCounter(SubmissionDTO submissionDTO, User user) throws UserException {
    user.setSubmitCount(user.getSubmitCount() + 1);
    if (Result.ACCEPTED.equals(Result.valueOf(submissionDTO.getResult()))) {
      user.setAcCount(user.getAcCount() + 1);
    }
    user.setAcRate(user.getAcCount() * 1.0 / user.getSubmitCount());
    userRepository.save(user);
  }

  private void problemStatCounter(SubmissionDTO submissionDTO, Problem problem) {
    problem.setSubmitCount(problem.getSubmitCount() + 1);
    if (Result.ACCEPTED.equals(Result.valueOf(submissionDTO.getResult()))) {
      problem.setAcceptCount(problem.getAcceptCount() + 1);
    }
    problem.setAcceptRate(problem.getAcceptCount() * 1.0 / problem.getSubmitCount());
    problemRepository.save(problem);
  }

  private void userTagStatCounter(
      User user,
      Set<Tag> tagSet,
      LocalDateTime lastSubmitTime,
      Integer scoreInc,
      Integer errorInc) {
    for (Tag tag : tagSet) {
      UserTagStat userTagStat =
          userTagStatRepository
              .findByUserIdAndTagId(user.getId(), tag.getId())
              .orElse(new UserTagStat(user.getId(), tag.getId()));
      userTagStat.setLastSubmitDate(lastSubmitTime);
      userTagStat.setScore(userTagStat.getScore() + scoreInc);
      userTagStat.setErrorTimes(userTagStat.getErrorTimes() + errorInc);
      userTagStatRepository.save(userTagStat);
    }
  }

  private void userProblemStatCounter(SubmissionDTO submissionDTO, User user, Problem problem) {
    Set<Tag> tagSet = problem.getTagList();

    UserProblemStat userProblemStat =
        userProblemStatRepository
            .findByUserIdAndProblemId(user.getId(), problem.getId())
            .orElse(new UserProblemStat(user.getId(), problem.getId()));
    userProblemStat.setLastSubmitDate(submissionDTO.getCreateDate());
    if (!userProblemStat.getPassed()) {
      if (Result.ACCEPTED.equals(Result.valueOf(submissionDTO.getResult()))) {
        if (userProblemStat.getErrorTimes() == 0) {
          userProblemStat.setScore(5);
          userTagStatCounter(user, tagSet, submissionDTO.getCreateDate(), 5, 0);
        } else {
          userProblemStat.setScore(userProblemStat.getScore() + 1);
          userTagStatCounter(user, tagSet, submissionDTO.getCreateDate(), 1, 0);
        }
        userProblemStat.setPassed(true);
      } else {
        if (userProblemStat.getErrorTimes() == 0) {
          userProblemStat.setScore(-2);
          userTagStatCounter(user, tagSet, submissionDTO.getCreateDate(), -2, 1);
        } else {
          userProblemStat.setScore(userProblemStat.getScore() - 1);
          userTagStatCounter(user, tagSet, submissionDTO.getCreateDate(), -1, 1);
        }
        userProblemStat.setErrorTimes(userProblemStat.getErrorTimes() + 1);
      }
      userProblemStatRepository.save(userProblemStat);
    }
  }

  private JudgeResult judge(Submission submission, Problem problem) throws SubmissionException {
    HashMap<String, Object> reqBody = new HashMap<>();
    reqBody.put("src", submission.getCode().replace("\\n", "\n"));
    reqBody.put("language_config", LanguageConfig.getLanguageConfig(submission.getLanguage()));
    reqBody.put("max_cpu_time", problem.getTimeLimit());
    reqBody.put("max_memory", problem.getRamLimit() * 1024 * 1024);
    reqBody.put("test_case_id", problem.getId());
    reqBody.put("output", "true");
    String reqBodyJson = JSON.toJSON(reqBody).toString();
    String response;
    try {
      response = http.post(judgeURL, reqBodyJson);
    } catch (IOException e) {
      throw new SubmissionException(JudgeException.POST_ERROR);
    }
    JSONObject resBodyJson = JSON.parseObject(response);
    String err = resBodyJson.getString("err");
    JudgeResult result = new JudgeResult();
    if (!CommonUtil.isNull(err)) {
      if (COMPILE_ERROR.equals(err)) {
        result.setResult(Result.COMPILE_ERROR);
      } else if (JUDGE_CLIENT_ERROR.equals(err)) {
        result.setResult(Result.JUDGE_CLIENT_ERROR);
      } else {
        result.setResult(Result.SYSTEM_ERROR);
      }
      result.setMessage(resBodyJson.getString("data"));
      return result;
    }
    List<JudgeResponse> data = resBodyJson.getJSONArray("data").toJavaList(JudgeResponse.class);
    Integer maxMemory = 0;
    Integer maxCPUTime = 0;
    Integer maxRealTime = 0;
    Integer passedCount = 0;
    Integer wrongAnswerCount = 0;
    Integer cpuTimeLimitExceededCount = 0;
    Integer timeLimitExceededCount = 0;
    Integer memoryLimitExceededCount = 0;
    result.setResult(Result.ACCEPTED);
    boolean resultResolved = false;
    for (JudgeResponse res : data) {
      if (res.getCpu_time() > maxCPUTime) {
        maxCPUTime = res.getCpu_time();
      }
      if (res.getMemory() > maxMemory) {
        maxMemory = res.getMemory();
      }
      if (res.getReal_time() > maxRealTime) {
        maxRealTime = res.getReal_time();
      }

      switch (res.getResult()) {
        case 0:
          passedCount++;
          break;
        case -1:
          wrongAnswerCount++;
          break;
        case 1:
          cpuTimeLimitExceededCount++;
          break;
        case 2:
          timeLimitExceededCount++;
          break;
        case 3:
          memoryLimitExceededCount++;
        default:
      }

      if (!resultResolved) {
        Integer r = res.getResult();
        if (r == 1 || r == 2 || r == 3 || r == 4 || r == 5) {
          result.setResult(integerToResult(r));
          resultResolved = true;
        }

        if (r == -1) {
          result.setResult(integerToResult(r));
          resultResolved = true;
        }
      }
    }
    result.setCpuTime(maxCPUTime);
    result.setMemory(maxMemory);
    result.setRealTime(maxRealTime);
    result.setTotalCount(data.size());
    result.setPassedCount(passedCount);
    result.setWrongAnswerCount(wrongAnswerCount);
    result.setCpuTimeLimitExceededCount(cpuTimeLimitExceededCount);
    result.setMemoryLimitExceededCount(memoryLimitExceededCount);
    result.setTimeLimitExceededCount(timeLimitExceededCount);
    return result;
  }

  private void requireContestUser(Contest contest, Optional<RankingUser> rankingUserOptional) throws ContestException {
    if (!rankingUserOptional.isPresent()) {
      OpenType type = contest.getOpenType();
      switch (type) {
        case PUBLIC:
          throw new ContestException(ContestException.NOT_PUBLIC_CONTEST_USER);
        case SECRET_WITH_PASSWORD:
          throw new ContestException(ContestException.NOT_PASS_CONTEST_USER);
        case SECRET_WITHOUT_PASSWORD:
          throw new ContestException(ContestException.NOT_PASS_CONTEST_USER);
        default:
          throw new ContestException(ContestException.NOT_PUBLIC_CONTEST_USER);
      }
    }
  }

  private void requireBeforeFrozen(Contest contest, Submission submission)
      throws SubmissionException {
    if (!submission.getIsBeforeFrozen() && contest.getFrozen()) {
      throw new SubmissionException(SubmissionException.RANKING_FROZEN);
    }
  }

  private void requireContestOnGoing(Contest contest) throws ContestException {
    if (!ContestStatus.PROCESSING.equals(contest.getStatus())
        || (LocalDateTime.now().isBefore(contest.getStartDate())
            || LocalDateTime.now().isAfter(contest.getEndDate()))) {
      throw new ContestException(ContestException.CONTEST_NOT_GOING);
    }
  }

  private Result integerToResult(Integer integer) {
    switch (integer) {
      case -1:
        return Result.WRONG_ANSWER;
      case 1:
        return Result.CPU_TIME_LIMIT_EXCEEDED;
      case 2:
        return Result.TIME_LIMIT_EXCEEDED;
      case 3:
        return Result.MEMORY_LIMIT_EXCEEDED;
      case 4:
        return Result.RUNTIME_ERROR;
      case 5:
        return Result.SYSTEM_ERROR;
      default:
        return Result.SYSTEM_ERROR;
    }
  }
}
