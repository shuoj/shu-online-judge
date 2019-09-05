package cn.kastner.oj.service;

import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.dto.ProblemDTO;
import cn.kastner.oj.exception.ProblemException;
import cn.kastner.oj.query.ProblemQuery;

import java.util.List;

public interface ProblemService {

  List<ProblemDTO> findProblemNoCriteria(Integer page, Integer size);

  PageDTO<ProblemDTO> findProblemCriteria(Integer page, Integer size, ProblemQuery problemQuery);

  ProblemDTO findProblemById(String id) throws ProblemException;

  ProblemDTO findProblemByTitle(String title);

  ProblemDTO create(ProblemDTO problemDTO) throws ProblemException;

  ProblemDTO update(ProblemDTO problemDTO) throws ProblemException;

  ProblemDTO delete(String id) throws ProblemException;
}
