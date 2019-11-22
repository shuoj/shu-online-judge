package cn.kastner.oj.service;

import cn.kastner.oj.domain.enums.Result;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.dto.SubmissionDTO;
import cn.kastner.oj.exception.ContestException;
import cn.kastner.oj.exception.ProblemException;
import cn.kastner.oj.exception.SubmissionException;
import cn.kastner.oj.exception.UserException;
import cn.kastner.oj.query.SubmissionQuery;

import java.util.List;

public interface SubmissionService {

  SubmissionDTO findById(String id) throws SubmissionException, ContestException;

    PageDTO<SubmissionDTO> findByContest(String id, Integer page, Integer size)
      throws ContestException;

  PageDTO<SubmissionDTO> findByUser(Integer page, Integer size, Boolean isPractice);

    List<SubmissionDTO> findByPracticeProblem(String problemId) throws ProblemException;

    List<SubmissionDTO> findByContestProblem(String contestId, String problemId) throws ProblemException, ContestException;

  SubmissionDTO createContestSubmission(SubmissionDTO submissionDTO)
      throws ContestException, ProblemException, SubmissionException;

  SubmissionDTO createPracticeSubmission(SubmissionDTO submissionDTO)
      throws ProblemException, SubmissionException;

  SubmissionDTO rejudgeSubmission(String id, Result result) throws SubmissionException;

  PageDTO<SubmissionDTO> findAll(Integer page, Integer size, SubmissionQuery submissionQuery)
      throws ProblemException, UserException;

  void counter(SubmissionDTO submissionDTO) throws ProblemException, UserException;
}
