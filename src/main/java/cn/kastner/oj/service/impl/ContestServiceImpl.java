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
import cn.kastner.oj.query.RankingQuery;
import cn.kastner.oj.repository.*;
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
import java.util.stream.Collectors;

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
    Set<RankingUser> rankingUserList = rankingUserRepository.findByContest(contest);
    for (RankingUser rankingUser : rankingUserList) {
      timeCostRepository.deleteAllByRankingUser(rankingUser);
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
    Optional<RankingUser> rankingUserOptional = rankingUserRepository.findByContestAndUser(contest, user);
    if (!CommonUtil.isAdmin(user) && !rankingUserOptional.isPresent()) {
      if (ContestType.SECRET_WITH_PASSWORD.equals(contest.getContestType())) {
        throw new ContestException(ContestException.NOT_PASS_CONTEST_USER);
      } else if (ContestType.SECRET_WITHOUT_PASSWORD.equals(contest.getContestType())) {
        throw new ContestException(ContestException.CANNOT_JOIN);
      }
    }

    return mapper.entityToDTO(contest);
  }

  @Override
  public List<RankingUserDTO> addUsersByGroups(List<String> groupIdList, String contestId)
      throws ContestException {
    Contest contest =
        contestRepository
            .findById(contestId)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    Set<User> userSet = rankingUserRepository
        .findByContest(contest)
        .stream()
        .map(RankingUser::getUser)
        .collect(Collectors.toSet());

    List<RankingUser> addRankingUserList = new ArrayList<>();
    List<Group> groupList = groupRepository.findAllById(groupIdList);
    for (Group group : groupList) {
      for (User user : group.getUserSet()) {
        if (!userSet.contains(user)) {
          addRankingUserList.add(RankingUserFactory.create(user, contest, group));
        }
      }
    }
    return mapper.toRankingUserDTOs(rankingUserRepository.saveAll(addRankingUserList));
  }

  @Override
  public List<RankingUserDTO> getUsers(String id) throws ContestException {
    Contest contest =
        contestRepository
            .findById(id)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));
    return mapper.toRankingUserDTOs(new ArrayList<>(rankingUserRepository.findByContest(contest)));
  }

  @Override
  public List<RankingUserDTO> addUsers(List<String> userIdList, String contestId) throws ContestException {
    Contest contest =
        contestRepository
            .findById(contestId)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    Set<User> userSet = rankingUserRepository
        .findByContest(contest)
        .stream()
        .map(RankingUser::getUser)
        .collect(Collectors.toSet());
    List<RankingUser> addRankingUserList = new ArrayList<>();
    List<User> userList = userRepository.findAllById(userIdList);
    for (User user : userList) {
      if (!userSet.contains(user)) {
        RankingUser rankingUser = RankingUserFactory.create(user, contest, null);
        timeCostRepository.saveAll(rankingUser.getTimeList());
        addRankingUserList.add(rankingUser);
      }
    }
    return mapper.toRankingUserDTOs(rankingUserRepository.saveAll(addRankingUserList));
  }

  @Override
  public void deleteUsers(List<String> rankingUserIdList, String contestId)
      throws ContestException {
    contestRepository
            .findById(contestId)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));
    List<RankingUser> rankingUserList = rankingUserRepository.findAllById(rankingUserIdList);
    for (RankingUser rankingUser : rankingUserList) {
      timeCostRepository.deleteAllByRankingUser(rankingUser);
      rankingUserRepository.delete(rankingUser);
    }
  }

  @Override
  public PageDTO<ContestDTO> findCriteria(Integer page, Integer size, ContestQuery contestQuery) throws ContestException {
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

    if (!addedContestProblemList.isEmpty()) {
      Set<RankingUser> rankingUserList = rankingUserRepository.findByContest(contest);
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
      rankingUserRepository.saveAll(rankingUserList);
    }

    return mapper.toProblemDTOs(addedContestProblemList.stream().map(ContestProblem::getProblem).collect(Collectors.toList()));
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
      Set<RankingUser> rankingUserList = rankingUserRepository.findByContest(contest);
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
      rankingUserRepository.saveAll(rankingUserList);
    }
  }

  @Override
  public void deleteProblems(List<String> problemIdList, String contestId) throws ContestException {

    Contest contest =
        contestRepository
            .findById(contestId)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    List<Problem> pl = problemRepository.findAllById(problemIdList);
    List<ContestProblem> contestProblemList = contestProblemRepository.findAllByProblemAndContest(pl, contest);
    timeCostRepository.deleteAllByContestProblem(contestProblemList);
    contestProblemRepository.deleteAll(contestProblemList);
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
      case START_IN_ADVANCE:
        setContestStatus(contest, ContestStatus.PROCESSING);
        break;
      case END_IN_ADVANCE:
        setContestStatus(contest, ContestStatus.ENDED);
        break;
      default:
        throw new ContestException(ContestException.BAD_CONTEST_STATUS);
    }
    return mapper.entityToDTO(contest);
  }

  private void setContestStatus(Contest contest, ContestStatus status) throws ContestException {
    switch (status) {
      case PROCESSING:
        if (contest.getStatus() != ContestStatus.NOT_STARTED) {
          throw new ContestException(ContestException.CAN_ONLY_CHANGE_FROM_NOT_STARTED);
        }
        contest.setStatus(ContestStatus.PROCESSING);
        contest.setStartDate(LocalDateTime.now());
        contestRepository.save(contest);
        break;
      case ENDED:
        contest.setStatus(ContestStatus.ENDED);
        contest.setEndDate(LocalDateTime.now());
        setProblemsVisible(contest);
        contestRepository.save(contest);
        break;
      default:
        throw new ContestException(ContestException.BAD_CONTEST_STATUS);
    }
  }

  @Override
  public void joinContest(String id, String password) throws ContestException {
    User user = UserContext.getCurrentUser();
    Contest contest =
        contestRepository
            .findById(id)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    switch (contest.getContestType()) {
      case PUBLIC:
        addUserToRanking(contest, user);
        break;
      case SECRET_WITHOUT_PASSWORD:
        throw new ContestException(ContestException.CANNOT_JOIN);
      case SECRET_WITH_PASSWORD:
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, contest.getPassword())) {
          throw new ContestException(ContestException.BAD_PASSWORD);
        }
        addUserToRanking(contest, user);
        break;
      default:
    }

  }

  private void addUserToRanking(Contest contest, User user) {
    Set<User> userSet = rankingUserRepository.findByContest(contest).stream().map(RankingUser::getUser).collect(Collectors.toSet());
    if (!userSet.contains(user)) {
      RankingUser rankingUser = RankingUserFactory.create(user, contest, null);
      timeCostRepository.saveAll(rankingUser.getTimeList());
      rankingUserRepository.save(rankingUser);
    }
  }

  @Override
  public RankingDTO getRanking(String id, RankingQuery query) throws ContestException {
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
      if (null != rankingUserList && !rankingUserList.isEmpty()) {
        if (null != query.getGroupId()) {
          rankingUserList = rankingUserList.stream().filter(
              rankingUserDTO -> query.getGroupId().equals(rankingUserDTO.getGroupId())
          ).collect(Collectors.toList());
        } else if (null != query.getTeacherId()) {
          rankingUserList = rankingUserList.stream().filter(
              rankingUserDTO -> query.getTeacherId().equals(rankingUserDTO.getTeacherId())
          ).collect(Collectors.toList());
        }
      }
      rankingDTO.setRankingUserList(rankingUserList);
      return rankingDTO;
    } else if (contest.getStatus() == ContestStatus.ENDED) {
      Set<RankingUser> rankingUserList = rankingUserRepository.findByContest(contest);
      for (RankingUser ru : rankingUserList) {
        ru.setTimeList(timeCostRepository.findByRankingUser(ru));
      }
      if (!rankingUserList.isEmpty()) {
        if (null != query.getGroupId()) {
          rankingUserList = rankingUserList.stream().filter(
              rankingUserDTO -> query.getGroupId().equals(rankingUserDTO.getGroupId())
          ).collect(Collectors.toSet());
        } else if (null != query.getTeacherId()) {
          rankingUserList = rankingUserList.stream().filter(
              rankingUserDTO -> query.getTeacherId().equals(rankingUserDTO.getTeacherId())
          ).collect(Collectors.toSet());
        }
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
