package cn.kastner.oj.service;

import cn.kastner.oj.domain.enums.ContestOption;
import cn.kastner.oj.dto.*;
import cn.kastner.oj.exception.ContestException;
import cn.kastner.oj.exception.ProblemException;
import cn.kastner.oj.query.ContestQuery;
import cn.kastner.oj.query.RankingQuery;

import java.util.List;

public interface ContestService {

  ContestDTO create(ContestDTO contestDTO) throws ContestException;

  void delete(String id) throws ContestException;

  ContestDTO update(ContestDTO contestDTO) throws ContestException;

  ContestDTO partUpdate(ContestDTO contestDTO) throws ContestException;

  ContestDTO findById(String id) throws ContestException;

  List<RankingUserDTO> addUsersByGroups(List<String> groupIdList, String contestId)
      throws ContestException;

  List<RankingUserDTO> getUsers(String id) throws ContestException;

  List<RankingUserDTO> addUsers(List<String> userIdList, String contestId) throws ContestException;

  void deleteUsers(List<String> userIdList, String id) throws ContestException;

  PageDTO<ContestDTO> findCriteria(Integer page, Integer size, ContestQuery contestQuery) throws ContestException;

  List<ProblemDTO> addProblems(List<String> problemIdList, String contestId)
      throws ContestException, ProblemException;

  void addProblem(String problemId, String contestId, Integer score) throws ContestException, ProblemException;

  void deleteProblems(List<String> problemIdList, String contestId)
      throws ContestException;

  List<ProblemDTO> findAllProblems(String id) throws ContestException;

  ProblemDTO findOneProblem(String contestId, String problemId) throws ContestException, ProblemException;

  ContestDTO setContestStatus(String id, ContestOption option) throws ContestException;

  void joinContest(String id, String password) throws ContestException;

  RankingDTO getRanking(String id, RankingQuery query) throws ContestException;
}
