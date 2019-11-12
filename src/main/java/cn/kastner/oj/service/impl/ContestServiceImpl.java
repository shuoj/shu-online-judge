package cn.kastner.oj.service.impl;

import cn.kastner.oj.constant.EntityName;
import cn.kastner.oj.domain.*;
import cn.kastner.oj.domain.enums.ContestOption;
import cn.kastner.oj.domain.enums.ContestStatus;
import cn.kastner.oj.domain.enums.ContestType;
import cn.kastner.oj.domain.enums.JudgeType;
import cn.kastner.oj.domain.security.UserContext;
import cn.kastner.oj.dto.*;
import cn.kastner.oj.exception.ContestException;
import cn.kastner.oj.exception.ProblemException;
import cn.kastner.oj.factory.RankingUserFactory;
import cn.kastner.oj.query.ContestQuery;
import cn.kastner.oj.repository.*;
import cn.kastner.oj.security.JwtUser;
import cn.kastner.oj.security.JwtUserFactory;
import cn.kastner.oj.service.ContestService;
import cn.kastner.oj.util.CommonUtil;
import cn.kastner.oj.util.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class ContestServiceImpl implements ContestService {

  private final ContestRepository contestRepository;

  private final TimeCostRepository timeCostRepository;

  private final ProblemRepository problemRepository;

  private final UserRepository userRepository;

  private final ContestProblemRepository contestProblemRepository;

  private final RankingUserRepository rankingUserRepository;

  private final GroupRepository groupRepository;

  private final IndexSequenceRepository indexSequenceRepository;

  private final SubmissionRepository submissionRepository;

  private final RedisTemplate redisTemplate;

  private final DTOMapper mapper;

  @Autowired
  public ContestServiceImpl(
      ContestRepository contestRepository,
      TimeCostRepository timeCostRepository,
      ProblemRepository problemRepository,
      UserRepository userRepository,
      ContestProblemRepository contestProblemRepository,
      RankingUserRepository rankingUserRepository,
      GroupRepository groupRepository,
      IndexSequenceRepository indexSequenceRepository,
      SubmissionRepository submissionRepository, RedisTemplate redisTemplate,
      DTOMapper mapper) {
    this.contestRepository = contestRepository;
    this.timeCostRepository = timeCostRepository;
    this.problemRepository = problemRepository;
    this.userRepository = userRepository;
    this.contestProblemRepository = contestProblemRepository;
    this.rankingUserRepository = rankingUserRepository;
    this.groupRepository = groupRepository;
    this.indexSequenceRepository = indexSequenceRepository;
    this.submissionRepository = submissionRepository;
    this.redisTemplate = redisTemplate;
    this.mapper = mapper;
  }

  @Override
  public ContestDTO create(ContestDTO contestDTO) throws ContestException {
    User user = UserContext.getCurrentUser();

    Optional<Contest> contestExist = contestRepository.findByName(contestDTO.getName());
    if (contestExist.isPresent()) {
      throw new ContestException(ContestException.HAVE_SAME_NAME_CONTEST);
    }

    Contest contest = mapper.dtoToEntity(contestDTO);

    if (contest.getStartDate().isEqual(LocalDateTime.now())
        || contest.getStartDate().isBefore(LocalDateTime.now())) {
      throw new ContestException(ContestException.START_TIME_IS_EARLY_THAN_NOW);
    }

    if (contest.getStartDate().isEqual(contest.getEndDate())
        || contest.getStartDate().isAfter(contest.getEndDate())) {
      throw new ContestException(ContestException.START_TIME_IS_AFTER_THAN_END_TIME);
    }

    requirePassword(contest);

    contest.setCreateDate(LocalDateTime.now());
    contest.setAuthor(user);
    IndexSequence sequence = indexSequenceRepository.findByName(EntityName.CONTEST);
    contest.setIdx(sequence.getNextIdx());
    ContestDTO dto = mapper.entityToDTO(contestRepository.save(contest));
    sequence.setNextIdx(contest.getIdx() + 1);
    indexSequenceRepository.save(sequence);
    return dto;
  }

  @Override
  public void delete(String id) throws ContestException {
    Contest contest =
        contestRepository
            .findById(id)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));
    List<RankingUser> rankingUserList = rankingUserRepository.findByContest(contest);
    for (RankingUser rankingUser : rankingUserList) {
      timeCostRepository.deleteByRankingUser(rankingUser);
    }
    submissionRepository.deleteAllByContest(contest);
    rankingUserRepository.deleteAll(rankingUserList);
    contestProblemRepository.deleteAllByContest(contest);
    contestRepository.delete(contest);
  }

  @Override
  public ContestDTO update(ContestDTO contestDTO) throws ContestException {
    User user = UserContext.getCurrentUser();
    Contest contest =
        contestRepository
            .findById(contestDTO.getId())
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    if (ContestStatus.ENDED.equals(contest.getStatus())) {
      throw new ContestException(ContestException.CONTEST_IS_ENDED);
    }

    if (!ContestStatus.PROCESSING.equals(contest.getStatus())) {
      if (null != contestDTO.getName()) {
        Optional<Contest> contestOptional = contestRepository.findByName(contestDTO.getName());
        if (contestOptional.isPresent()
            && !contestOptional.get().getId().equals(contestDTO.getId())) {
          throw new ContestException(ContestException.HAVE_SAME_NAME_CONTEST);
        }
        contest.setName(contestDTO.getName());
      }

      if (null != contestDTO.getCouldShare()) {
        contest.setCouldShare(contestDTO.getCouldShare());
      }

      if (null != contestDTO.getContestType()) {
        contest.setContestType(ContestType.valueOf(contestDTO.getContestType()));
        requirePassword(contest);
      }

      if (null != contestDTO.getJudgeType()) {
        contest.setJudgeType(JudgeType.valueOf(contestDTO.getJudgeType()));
      }

      if (null != contestDTO.getStatus()) {
        // set status, enable, ranking
        setContestStatus(contest, ContestStatus.valueOf(contestDTO.getStatus()));
      }

      if (null != contestDTO.getDescription()) {
        contest.setDescription(contestDTO.getDescription());
      }

      if (null != contestDTO.getStartDate()) {
        contest.setStartDate(contestDTO.getStartDate());
      }

      if (null != contestDTO.getPassword()) {
        contest.setPassword(contestDTO.getPassword());
      }
    }

    if (null != contestDTO.getEndDate()) {
      contest.setEndDate(contestDTO.getEndDate());
    }

    contest.setAuthor(user);
    return mapper.entityToDTO(contestRepository.save(contest));
  }

  @Override
  public ContestDTO partUpdate(ContestDTO contestDTO) throws ContestException {
    Contest contest =
        contestRepository
            .findById(contestDTO.getId())
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    Boolean visible = contestDTO.getVisible();
    if (null != visible) {
      contest.setVisible(visible);
    }
    return mapper.entityToDTO(contestRepository.save(contest));
  }

  @Override
  public ContestDTO findById(String id) throws ContestException {
    User user = UserContext.getCurrentUser();
    Contest contest =
        contestRepository
            .findById(id)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    if (!CommonUtil.isAdmin(user) && !contest.getUserSet().contains(user)) {
      if (ContestType.SECRET_WITH_PASSWORD.equals(contest.getContestType())) {
        throw new ContestException(ContestException.NOT_PASS_CONTEST_USER);
      } else if (ContestType.SECRET_WITHOUT_PASSWORD.equals(contest.getContestType())) {
        throw new ContestException(ContestException.CANNOT_JOIN);
      }
    }

    return mapper.entityToDTO(contest);
  }

  @Override
  public List<JwtUser> addUsersByGroups(List<String> groupIdList, String contestId)
      throws ContestException {
    Contest contest =
        contestRepository
            .findById(contestId)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    Set<User> userSet = contest.getUserSet();
    List<Group> groupList = groupRepository.findAllById(groupIdList);
    for (Group group : groupList) {
      userSet.addAll(group.getUserSet());
    }
    contest.setUserSet(userSet);
    contestRepository.save(contest);
    return JwtUserFactory.createList(userSet);
  }

  @Override
  public List<JwtUser> getUsers(String id) throws ContestException {
    Contest contest =
        contestRepository
            .findById(id)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));
    Set<User> userSet = contest.getUserSet();
    return JwtUserFactory.createList(userSet);
  }

  @Override
  public List<JwtUser> addUsers(List<String> userIdList, String contestId) throws ContestException {
    Contest contest =
        contestRepository
            .findById(contestId)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    Set<User> userSet = contest.getUserSet();
    List<User> userList = userRepository.findAllById(userIdList);
    for (User user : userList) {
      if (!userSet.contains(user)) {
        userSet.add(user);
        if (contest.getEnable()) {
          addUserToRanking(contest, user);
        }
      }
    }
    contest.setUserSet(userSet);
    contestRepository.save(contest);
    return JwtUserFactory.createList(userSet);
  }

  @Override
  public List<JwtUser> deleteUsers(List<String> userIdList, String contestId)
      throws ContestException {
    Contest contest =
        contestRepository
            .findById(contestId)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    Set<User> userSet = contest.getUserSet();
    List<User> deleteUserList = userRepository.findAllById(userIdList);
    userSet.removeAll(deleteUserList);

    contest.setUserSet(userSet);
    contestRepository.save(contest);
    return JwtUserFactory.createList(userSet);
  }

  @Override
  public PageDTO<ContestDTO> findCriteria(Integer page, Integer size, ContestQuery contestQuery) {
    User user = UserContext.getCurrentUser();
    Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "startDate");
    Specification<Contest> cs =
        (root, criteriaQuery, criteriaBuilder) -> {
          List<Predicate> predicateList = new ArrayList<>();

          String name = contestQuery.getName();
          if (null != name && !"".equals(name)) {
            predicateList.add(
                criteriaBuilder.like(root.get("name").as(String.class), "%" + name + "%"));
          }

          ContestStatus status = contestQuery.getStatus();
          if (null != status) {
            predicateList.add(
                criteriaBuilder.equal(root.get("status").as(ContestStatus.class), status));
          }

          ContestType type = contestQuery.getType();
          if (null != type) {
            predicateList.add(
                criteriaBuilder.equal(root.get("contestType").as(ContestType.class), type));
          }

          if (!CommonUtil.isAdmin(user)) {
            predicateList.add(criteriaBuilder.equal(root.get("visible").as(Boolean.class), true));
          }

          Predicate[] p = new Predicate[predicateList.size()];
          return criteriaBuilder.and(predicateList.toArray(p));
        };
    List<Contest> contestList = contestRepository.findAll(cs, pageable).getContent();
    for (Contest contest : contestList) {
      if ((contest.getStartDate().isBefore(LocalDateTime.now())
          || contest.getStartDate().isEqual(LocalDateTime.now()))
          && contest.getStatus().equals(ContestStatus.NOT_STARTED)) {
        setContestStatus(contest, ContestStatus.PROCESSING);
      }

      if ((contest.getEndDate().isBefore(LocalDateTime.now()))
          || (contest.getEndDate().isEqual(LocalDateTime.now()))) {
        setContestStatus(contest, ContestStatus.ENDED);
      }
    }
    contestList = contestRepository.saveAll(contestList);
    Long count = contestRepository.count(cs);
    List<ContestDTO> contestDTOList = mapper.toContestDTOs(contestList);
    return new PageDTO<>(page, size, count, contestDTOList);
  }

  @Override
  public List<ProblemDTO> addProblems(List<String> problemIdList, String contestId)
      throws ContestException, ProblemException {
    Contest contest =
        contestRepository
            .findById(contestId)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    List<ContestProblem> contestProblemList = contestProblemRepository.findByContest(contest);
    List<ContestProblem> addedContestProblemList = new ArrayList<>();
    List<Problem> problemList = getProblemList(contest);
    for (String problemId : problemIdList) {
      Problem problem =
          problemRepository
              .findById(problemId)
              .orElseThrow(() -> new ProblemException(ProblemException.NO_SUCH_PROBLEM));
      if (!problemList.contains(problem)) {
        ContestProblem contestProblem = new ContestProblem();
        contestProblem.setProblem(problem);
        contestProblem.setContest(contest);
        contestProblemList.add(contestProblem);
        addedContestProblemList.add(contestProblem);
        problem.setVisible(false);
        problem.setLastUsedDate(LocalDateTime.now());
        problemList.add(problem);
      }
    }
    contestProblemRepository.saveAll(contestProblemList);

    if (contest.getEnable() && !addedContestProblemList.isEmpty()) {
      List<RankingUser> rankingUserList = contest.getRankingUserList();
      for (RankingUser rankingUser : rankingUserList) {
        List<TimeCost> timeCostList = rankingUser.getTimeList();
        List<TimeCost> addTimeCostList = new ArrayList<>();
        for (ContestProblem contestProblem : addedContestProblemList) {

          TimeCost timeCost = new TimeCost();
          timeCost.setContestProblem(contestProblem);
          timeCost.setRankingUser(rankingUser);
          timeCost.setFrozen(true);
          addTimeCostList.add(timeCost);
        }

        timeCostList.addAll(timeCostRepository.saveAll(addTimeCostList));

        rankingUser.setTimeList(timeCostList);
      }
      contest.setRankingUserList(rankingUserRepository.saveAll(rankingUserList));
      contestRepository.save(contest);
    }

    return mapper.toProblemDTOs(problemList);
  }

  @Override
  public void addProblem(String problemId, String contestId, Integer score)
      throws ContestException, ProblemException {
    Contest contest =
        contestRepository
            .findById(contestId)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    if (!contest.getJudgeType().equals(JudgeType.DELAY)) {
      throw new ContestException(ContestException.WRONG_CONTEST_TYPE);
    }

    List<Problem> problemList = getProblemList(contest);
    Problem problem =
        problemRepository
            .findById(problemId)
            .orElseThrow(() -> new ProblemException(ProblemException.NO_SUCH_PROBLEM));
    ContestProblem contestProblem;
    if (!problemList.contains(problem)) {
      contestProblem = new ContestProblem();
      contestProblem.setProblem(problem);
      contestProblem.setContest(contest);
      contestProblem.setScore(score);
      problem.setVisible(false);
      problem.setLastUsedDate(LocalDateTime.now());
      problemList.add(problem);
      contestProblemRepository.save(contestProblem);
      if (contest.getEnable()) {
        List<RankingUser> rankingUserList = contest.getRankingUserList();
        for (RankingUser rankingUser : rankingUserList) {
          List<TimeCost> timelist = rankingUser.getTimeList();
          List<TimeCost> addTimeCostList = new ArrayList<>();

          TimeCost timeCost = new TimeCost();
          timeCost.setContestProblem(contestProblem);
          timeCost.setRankingUser(rankingUser);
          timeCost.setFrozen(true);

          addTimeCostList.add(timeCost);

          timelist.addAll(timeCostRepository.saveAll(addTimeCostList));

          rankingUser.setTimeList(timelist);
        }
        contest.setRankingUserList(rankingUserRepository.saveAll(rankingUserList));
        contestRepository.save(contest);
      }
    }
  }

  @Override
  public void deleteProblems(List<String> problemIdList, String contestId) throws ContestException {

    Contest contest =
        contestRepository
            .findById(contestId)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    List<Problem> pl = problemRepository.findAllById(problemIdList);
    contestProblemRepository.deleteAllByProblemAndContest(pl, contest);
    contestRepository.save(contest);
  }

  @Override
  public List<ProblemDTO> findAllProblems(String id) throws ContestException {
    User user = UserContext.getCurrentUser();
    Contest contest =
        contestRepository
            .findById(id)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));
    if (!CommonUtil.isAdmin(user) && ContestStatus.NOT_STARTED.equals(contest.getStatus())) {
      throw new ContestException(ContestException.CONTEST_NOT_GOING);
    }
    return mapper.toContestProblemDTOs(contest.getContestProblemSet());
  }

  @Override
  public ProblemDTO findOneProblem(String contestId, String problemId)
      throws ContestException, ProblemException {
    User user = UserContext.getCurrentUser();
    Contest contest =
        contestRepository
            .findById(contestId)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));
    if (!CommonUtil.isAdmin(user) && ContestStatus.NOT_STARTED.equals(contest.getStatus())) {
      throw new ContestException(ContestException.CONTEST_NOT_GOING);
    }
    Problem problem =
        problemRepository
            .findById(problemId)
            .orElseThrow(() -> new ProblemException(ProblemException.NO_SUCH_PROBLEM));
    ContestProblem contestProblem =
        contestProblemRepository.findByContestAndProblem(contest, problem);
    return mapper.entityToDTO(contestProblem);
  }

  @Override
  public ContestDTO setContestStatus(String id, ContestOption option) throws ContestException {

    Contest contest =
        contestRepository
            .findById(id)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    switch (option) {
      case ENABLE:
        setContestStatus(contest, ContestStatus.PROCESSING);
        break;
      case DISABLE:
        setContestStatus(contest, ContestStatus.ENDED);
        break;
      case RESET:
        setContestStatus(contest, ContestStatus.NOT_STARTED);
        break;
      default:
    }
    return mapper.entityToDTO(contest);
  }

  private void setContestStatus(Contest contest, ContestStatus status) {
    switch (status) {
      case NOT_STARTED:
        contest.setStatus(ContestStatus.NOT_STARTED);
        contest.setEnable(false);
        contestRepository.save(contest);
        rankingUserRepository.deleteAllByContest(contest);
        submissionRepository.deleteAllByContest(contest);
        break;
      case PROCESSING:
        boolean needClean = contest.getStatus() == ContestStatus.ENDED;
        contest.setStatus(ContestStatus.PROCESSING);
        contest.setEnable(true);
        contest.setStartDate(LocalDateTime.now());
        contestRepository.save(contest);
        if (needClean) {
          rankingUserRepository.deleteAllByContest(contest);
          submissionRepository.deleteAllByContest(contest);
        }
        // initialize rankingUserList
        List<RankingUser> rankingUserList = new ArrayList<>();
        for (User user : contest.getUserSet()) {
          RankingUser rankingUser = RankingUserFactory.create(user, contest);
          timeCostRepository.saveAll(rankingUser.getTimeList());
          rankingUserList.add(rankingUser);
        }
        rankingUserRepository.saveAll(rankingUserList);
        break;
      case ENDED:
        contest.setEnable(false);
        contest.setStatus(ContestStatus.ENDED);
        setProblemsVisible(contest);
        contestRepository.save(contest);
        break;
      default:
    }
  }

  @Override
  public Boolean joinContest(String id, String password) throws ContestException {
    User user = UserContext.getCurrentUser();
    Contest contest =
        contestRepository
            .findById(id)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    boolean result = false;
    switch (contest.getContestType()) {
      case PUBLIC:
        if (contest.getEnable()) {
          addUserToRanking(contest, user);
        }

        Set<User> ul = contest.getUserSet();
        ul.add(user);
        contest.setUserSet(ul);
        contestRepository.save(contest);
        result = true;
        break;
      case SECRET_WITHOUT_PASSWORD:
        break;
      case SECRET_WITH_PASSWORD:
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (encoder.matches(password, contest.getPassword())) {
          if (contest.getEnable()) {
            addUserToRanking(contest, user);
          }

          Set<User> userSet = contest.getUserSet();
          userSet.add(user);
          contest.setUserSet(userSet);
          contestRepository.save(contest);
          result = true;
        } else {
          result = false;
        }
        break;
      default:
    }

    return result;
  }

  private void addUserToRanking(Contest contest, User user) {
    List<RankingUser> rankingUserList = contest.getRankingUserList();
    RankingUser rankingUser = RankingUserFactory.create(user, contest);
    timeCostRepository.saveAll(rankingUser.getTimeList());
    rankingUserList.add(rankingUserRepository.save(rankingUser));
    contest.setRankingUserList(rankingUserList);
    contestRepository.save(contest);
  }

  @Override
  public RankingDTO getRanking(String id) throws ContestException {
    Optional<Contest> contestOptional = contestRepository.findById(id);
    if (!contestOptional.isPresent()) {
      throw new ContestException(ContestException.NO_SUCH_CONTEST);
    }
    Contest contest = contestOptional.get();
    RankingDTO rankingDTO = new RankingDTO();
    rankingDTO.setContestId(contest.getId());
    rankingDTO.setContestName(contest.getName());
    if (contest.getStatus() == ContestStatus.PROCESSING) {
      List<RankingUserDTO> rankingUserList =
          (List<RankingUserDTO>) redisTemplate.opsForValue().get("rankingUserList:" + contest.getId());
      for (RankingUserDTO rankingUserDTO :
          rankingUserList == null ? new ArrayList<RankingUserDTO>() : rankingUserList) {
        rankingUserDTO.setTimeList(
            (List<TimeCostDTO>)
                redisTemplate.opsForValue().get("timeCostList:" + rankingUserDTO.getId()));
      }
      rankingDTO.setRankingUserList(rankingUserList);
      return rankingDTO;
    } else if (contest.getStatus() == ContestStatus.ENDED) {
      List<RankingUser> rankingUserList = rankingUserRepository.findByContest(contest);
      for (RankingUser ru : rankingUserList) {
        ru.setTimeList(timeCostRepository.findByRankingUser(ru));
      }
      contest.setRankingUserList(rankingUserList);
      return mapper.contestToRankingDTO(contest);
    } else {
      throw new ContestException(ContestException.CONTEST_NOT_GOING);
    }

  }

  private void requirePassword(Contest contest) throws ContestException {
    if (ContestType.SECRET_WITH_PASSWORD.equals(contest.getContestType())
        && contest.getPassword() == null) {
      throw new ContestException(ContestException.NO_PASS_PROVIDED);
    }
  }

  private void setProblemsVisible(Contest contest) {
    List<Problem> problemList = getProblemList(contest);
    for (Problem problem : problemList) {
      problem.setVisible(true);
    }
    problemRepository.saveAll(problemList);
  }

  private List<Problem> getProblemList(Contest contest) {
    List<ContestProblem> contestProblemList = contestProblemRepository.findByContest(contest);
    List<Problem> problemList = new ArrayList<>();
    for (ContestProblem contestProblem : contestProblemList) {
      problemList.add(contestProblem.getProblem());
    }
    return problemList;
  }
}
