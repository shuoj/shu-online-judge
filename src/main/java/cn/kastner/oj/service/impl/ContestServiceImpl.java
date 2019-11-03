package cn.kastner.oj.service.impl;

import cn.kastner.oj.constant.EntityName;
import cn.kastner.oj.domain.*;
import cn.kastner.oj.domain.security.UserContext;
import cn.kastner.oj.dto.ContestDTO;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.dto.ProblemDTO;
import cn.kastner.oj.dto.RankingDTO;
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

  private final RankingRepository rankingRepository;

  private final GroupRepository groupRepository;

  private final IndexSequenceRepository indexSequenceRepository;

  private final DTOMapper mapper;

  @Autowired
  public ContestServiceImpl(
      ContestRepository contestRepository,
      TimeCostRepository timeCostRepository,
      ProblemRepository problemRepository,
      UserRepository userRepository,
      ContestProblemRepository contestProblemRepository,
      RankingUserRepository rankingUserRepository,
      RankingRepository rankingRepository, GroupRepository groupRepository,
      IndexSequenceRepository indexSequenceRepository,
      DTOMapper mapper) {
    this.contestRepository = contestRepository;
    this.timeCostRepository = timeCostRepository;
    this.problemRepository = problemRepository;
    this.userRepository = userRepository;
    this.contestProblemRepository = contestProblemRepository;
    this.rankingUserRepository = rankingUserRepository;
    this.rankingRepository = rankingRepository;
    this.groupRepository = groupRepository;
    this.indexSequenceRepository = indexSequenceRepository;
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
    Ranking ranking = new Ranking();
    ranking.setContest(contest);
    contest.setRanking(ranking);
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
    Ranking ranking = rankingRepository.findByContest(contest);
    List<RankingUser> rankingUserList = rankingUserRepository.findByRanking(ranking);
    for (RankingUser rankingUser : rankingUserList) {
      timeCostRepository.deleteByRankingUser(rankingUser);
    }
    rankingUserRepository.deleteAll(rankingUserList);
    rankingRepository.delete(ranking);
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
        if (contestOptional.isPresent() && !contestOptional.get().getId().equals(contestDTO.getId())) {
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
        problemList.add(problem);
      }
    }
    contestProblemRepository.saveAll(contestProblemList);

    if (contest.getEnable()) {
      Ranking ranking = contest.getRanking();
      List<RankingUser> rankingUserList = ranking.getRankingUserList();
      for (RankingUser rankingUser : rankingUserList) {
        List<TimeCost> timeListBefore = rankingUser.getTimeListBefore();
        List<TimeCost> timeListAfter = rankingUser.getTimeListAfter();
        List<TimeCost> addTimeCostListBefore = new ArrayList<>();
        List<TimeCost> addTimeCostListAfter = new ArrayList<>();
        for (ContestProblem contestProblem : addedContestProblemList) {

          TimeCost timeCostBefore = new TimeCost();
          timeCostBefore.setContestProblem(contestProblem);
          timeCostBefore.setRankingUser(rankingUser);
          timeCostBefore.setFrozen(true);
          addTimeCostListBefore.add(timeCostBefore);

          TimeCost timeCostAfter = new TimeCost();
          timeCostAfter.setContestProblem(contestProblem);
          timeCostAfter.setRankingUser(rankingUser);
          timeCostAfter.setFrozen(false);
          addTimeCostListAfter.add(timeCostAfter);

        }

        timeListBefore.addAll(timeCostRepository.saveAll(addTimeCostListBefore));
        timeListAfter.addAll(timeCostRepository.saveAll(addTimeCostListAfter));

        rankingUser.setTimeListAfter(timeListAfter);
        rankingUser.setTimeListBefore(timeListBefore);
      }
      ranking.setRankingUserList(rankingUserRepository.saveAll(rankingUserList));
      rankingRepository.save(ranking);
    }

    return mapper.toProblemDTOs(problemList);
  }

  @Override
  public List<ProblemDTO> deleteProblems(List<String> problemIdList, String contestId)
      throws ContestException {

    Contest contest =
        contestRepository
            .findById(contestId)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    List<Problem> pl = problemRepository.findAllById(problemIdList);
    contestProblemRepository.deleteAllByProblemAndContest(pl, contest);

    List<Problem> problemList = getProblemList(contest);
    contestRepository.save(contest);
    return mapper.toProblemDTOs(problemList);
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
    List<Problem> problemList = getProblemList(contest);
    return mapper.toProblemDTOs(problemList);
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
    return mapper.entityToDTO(contestRepository.save(contest));
  }

  private void setContestStatus(Contest contest, ContestStatus status) {
    Ranking ranking = contest.getRanking();
    switch (status) {
      case NOT_STARTED:
        contest.setStatus(ContestStatus.NOT_STARTED);
        contest.setEnable(false);
        rankingUserRepository.deleteAll(rankingUserRepository.findByRanking(ranking));
        contest.setRanking(ranking);
        break;
      case PROCESSING:
        contest.setStatus(ContestStatus.PROCESSING);
        contest.setEnable(true);
        contest.setStartDate(LocalDateTime.now());
        rankingUserRepository.deleteAll(rankingUserRepository.findByRanking(ranking));
        // initialize rankingUserList
        List<RankingUser> rankingUserList = new ArrayList<>();
        for (User user : contest.getUserSet()) {
          RankingUser rankingUser = RankingUserFactory.create(user, contest);
          rankingUserList.add(rankingUser);
        }
        ranking.setRankingUserList(rankingUserList);
        break;
      case ENDED:
        contest.setEnable(false);
        contest.setStatus(ContestStatus.ENDED);
        setProblemsVisible(contest);
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
    Ranking ranking = contest.getRanking();
    List<RankingUser> rankingUserList = ranking.getRankingUserList();
    RankingUser rankingUser = RankingUserFactory.create(user, contest);
    rankingUserList.add(rankingUserRepository.save(rankingUser));
    ranking.setRankingUserList(rankingUserList);
    rankingRepository.save(ranking);
  }

  @Override
  public RankingDTO getRanking(String id) throws ContestException {
    Optional<Contest> contestOptional = contestRepository.findById(id);
    if (!contestOptional.isPresent()) {
      throw new ContestException(ContestException.NO_SUCH_CONTEST);
    }
    Contest contest = contestOptional.get();
    Ranking ranking = contest.getRanking();
    List<RankingUser> rankingUserList = rankingUserRepository.findByRanking(ranking);
    for (RankingUser ru : rankingUserList) {
      List<TimeCost> timeCostList = timeCostRepository.findByRankingUserAndFrozen(ru, false);
      ru.setTimeListBefore(timeCostList);
    }
    ranking.setRankingUserList(rankingUserList);
    if (LocalDateTime.now().isBefore(contest.getStartDate().plusHours(4))) {
      return mapper.entityToDTO(ranking, false);
    } else {
      return mapper.entityToDTO(ranking, true);
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
