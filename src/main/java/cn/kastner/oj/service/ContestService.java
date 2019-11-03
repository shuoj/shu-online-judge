package cn.kastner.oj.service;

import cn.kastner.oj.domain.ContestOption;
import cn.kastner.oj.dto.ContestDTO;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.dto.ProblemDTO;
import cn.kastner.oj.dto.RankingDTO;
import cn.kastner.oj.exception.ContestException;
import cn.kastner.oj.exception.ProblemException;
import cn.kastner.oj.query.ContestQuery;
import cn.kastner.oj.security.JwtUser;

import java.util.List;

public interface ContestService {

  ContestDTO create(ContestDTO contestDTO) throws ContestException;

  void delete(String id) throws ContestException;

  ContestDTO update(ContestDTO contestDTO) throws ContestException;

  ContestDTO partUpdate(ContestDTO contestDTO) throws ContestException;

  ContestDTO findById(String id) throws ContestException;

  List<JwtUser> addUsersByGroups(List<String> groupIdList, String contestId)
      throws ContestException;

  List<JwtUser> getUsers(String id) throws ContestException;

  List<JwtUser> addUsers(List<String> userIdList, String contestId) throws ContestException;

  List<JwtUser> deleteUsers(List<String> userIdList, String id) throws ContestException;

  PageDTO<ContestDTO> findCriteria(Integer page, Integer size, ContestQuery contestQuery);

  List<ProblemDTO> addProblems(List<String> problemIdList, String contestId)
      throws ContestException, ProblemException;

  List<ProblemDTO> deleteProblems(List<String> problemIdList, String contestId)
      throws ContestException;

  List<ProblemDTO> findAllProblems(String id) throws ContestException;

  ContestDTO setContestStatus(String id, ContestOption option) throws ContestException;

  Boolean joinContest(String id, String password) throws ContestException;

  RankingDTO getRanking(String id) throws ContestException;
}
