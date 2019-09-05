package cn.kastner.oj.service.impl;

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
import java.util.*;

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
      DTOMapper mapper) {
    this.contestRepository = contestRepository;
    this.timeCostRepository = timeCostRepository;
    this.problemRepository = problemRepository;
    this.userRepository = userRepository;
    this.contestProblemRepository = contestProblemRepository;
    this.rankingUserRepository = rankingUserRepository;
    this.groupRepository = groupRepository;
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

    requirePassword(contest);

    contest.setCreateDate(LocalDateTime.now());
    contest.setAuthor(user);
    Ranking ranking = new Ranking();
    ranking.setContest(contest);
    contest.setRanking(ranking);
    return mapper.entityToDTO(contestRepository.save(contest));
  }

  @Override
  public ContestDTO delete(String id) throws ContestException {
    Optional<Contest> contestOptional = contestRepository.findById(id);
    if (!contestOptional.isPresent()) {
      throw new ContestException(ContestException.NO_SUCH_CONTEST);
    }
    ContestDTO contestDTO = mapper.entityToDTO(contestOptional.get());
    contestRepository.delete(contestOptional.get());
    return contestDTO;
  }

  @Override
  public ContestDTO update(ContestDTO contestDTO) throws ContestException {
    User user = UserContext.getCurrentUser();
    Optional<Contest> contestOptional = contestRepository.findByName(contestDTO.getName());
    if (contestOptional.isPresent() && !contestOptional.get().getId().equals(contestDTO.getId())) {
      throw new ContestException(ContestException.HAVE_SAME_NAME_CONTEST);
    }
    Contest contest = mapper.dtoToEntity(contestDTO);

    requirePassword(contest);

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

    if (!CommonUtil.isAdmin(user) && contest.getUserSet().contains(user)) {
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
    userSet.addAll(userList);

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
    List<Problem> problemList = getProblemList(contest);
    for (String problemId : problemIdList) {
      Problem problem = problemRepository.findById(problemId)
          .orElseThrow(() -> new ProblemException(ProblemException.NO_SUCH_PROBLEM));
      if (!problemList.contains(problem)) {
        ContestProblem contestProblem = new ContestProblem();
        contestProblem.setProblem(problem);
        contestProblem.setContest(contest);
        contestProblemList.add(contestProblem);
        problem.setVisible(false);
        problemList.add(problem);
      }
    }
    contestProblemRepository.saveAll(contestProblemList);
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

    Ranking ranking = contest.getRanking();
    switch (option) {
      case ENABLE:
        contest.setStatus(ContestStatus.PROCESSING);
        contest.setEnable(true);
        rankingUserRepository.deleteAll(rankingUserRepository.findByRanking(ranking));
        // initialize rankingUserList
        List<RankingUser> rankingUserList = new ArrayList<>();
        Iterator<User> userIterator = contest.getUserSet().iterator();
        while (userIterator.hasNext()) {
          User user = userIterator.next();
          RankingUser rankingUser = RankingUserFactory.create(user, contest);
          rankingUserList.add(rankingUser);
        }
        ranking.setRankingUserList(rankingUserList);
        break;
      case DISABLE:
        contest.setStatus(ContestStatus.ENDED);
        contest.setEnable(false);
        setProblemsVisible(contest);
        break;
      case RESET:
        contest.setStatus(ContestStatus.NOT_STARTED);
        contest.setEnable(false);
        rankingUserRepository.deleteAll(rankingUserRepository.findByRanking(ranking));
        contest.setRanking(ranking);
        break;
      default:
        break;
    }
    return mapper.entityToDTO(contestRepository.save(contest));
  }

  @Override
  public Boolean joinContest(String id, String password) throws ContestException {
    User user = UserContext.getCurrentUser();
    Contest contest =
        contestRepository
            .findById(id)
            .orElseThrow(() -> new ContestException(ContestException.NO_SUCH_CONTEST));

    switch (contest.getContestType()) {
      case PUBLIC:
        if (contest.getEnable()) {
          Ranking ranking = contest.getRanking();
          List<RankingUser> rankingUserList = ranking.getRankingUserList();
          RankingUser rankingUser = RankingUserFactory.create(user, contest);
          rankingUserList.add(rankingUser);
          ranking.setRankingUserList(rankingUserList);
        }

        Set<User> ul = contest.getUserSet();
        ul.add(user);
        contest.setUserSet(ul);
        contestRepository.save(contest);
        return true;
      case SECRET_WITHOUT_PASSWORD:
        return false;
      case SECRET_WITH_PASSWORD:
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encryptPassword = encoder.encode(password);
        if (contest.getPassword().equals(encryptPassword)) {
          if (contest.getEnable()) {
            Ranking ranking = contest.getRanking();
            List<RankingUser> rankingUserList = ranking.getRankingUserList();
            RankingUser rankingUser = RankingUserFactory.create(user, contest);
            rankingUserList.add(rankingUser);
            ranking.setRankingUserList(rankingUserList);
          }

          Set<User> userSet = contest.getUserSet();
          userSet.add(user);
          contest.setUserSet(userSet);
          contestRepository.save(contest);
          return true;
        }
        return false;
      default:
        return false;
    }
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
