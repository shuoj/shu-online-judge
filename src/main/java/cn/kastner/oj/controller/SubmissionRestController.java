package cn.kastner.oj.controller;

import cn.kastner.oj.domain.enums.Result;
import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.dto.SubmissionDTO;
import cn.kastner.oj.exception.AppException;
import cn.kastner.oj.exception.ValidateException;
import cn.kastner.oj.query.SubmissionQuery;
import cn.kastner.oj.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1")
public class SubmissionRestController {

  private final SubmissionService submissionService;

  @Autowired
  public SubmissionRestController(SubmissionService submissionService) {
    this.submissionService = submissionService;
  }

  @GetMapping(value = "/submissions")
  public PageDTO<SubmissionDTO> getSubmissions(
          @RequestParam(defaultValue = "0") Integer page,
          @RequestParam(defaultValue = "10") Integer size,
          SubmissionQuery submissionQuery)
          throws AppException {
    return submissionService.findAll(page, size, submissionQuery);
  }

  /**
   * 获取提交
   *
   * @param id 提交id
   */
  @GetMapping(value = "/submissions/{id}")
  public SubmissionDTO getSubmission(@PathVariable String id) throws AppException {
    return submissionService.findById(id);
  }

  @GetMapping(value = "/contests/{id}/submissions")
  public PageDTO<SubmissionDTO> getSubmissions(
          @PathVariable String id,
          @RequestParam(defaultValue = "0") Integer page,
          @RequestParam(defaultValue = "10") Integer size)
          throws AppException {
    return submissionService.findByContest(id, page, size);
  }

  @GetMapping(value = "/contests/{contestId}/problems/{problemId}/submissions")
  public List<SubmissionDTO> getContestSubmissions(
          @PathVariable String contestId, @PathVariable String problemId) throws AppException {
    return submissionService.findByContestProblem(contestId, problemId);
  }

  @PostMapping(value = "/contests/{contestId}/problems/{problemId}/submissions")
  public SubmissionDTO createContestSubmissions(
          @PathVariable String contestId,
          @PathVariable String problemId,
          @Validated @RequestBody SubmissionDTO submissionDTO,
          BindingResult bindingResult)
          throws AppException {
    if (bindingResult.hasErrors())
      throw new ValidateException(bindingResult.getFieldError().getDefaultMessage());
    submissionDTO.setProblemId(problemId);
    submissionDTO.setContestId(contestId);
    return submissionService.createContestSubmission(submissionDTO);
  }

  @GetMapping(value = "/problems/{id}/submissions")
  public List<SubmissionDTO> getPracticeSubmissions(@PathVariable String id) throws AppException {
    return submissionService.findByPracticeProblem(id);
  }

  @PostMapping(value = "/problems/{id}/submissions")
  public SubmissionDTO createPracticeSubmissions(
          @PathVariable String id,
          @Validated @RequestBody SubmissionDTO submissionDTO,
          BindingResult bindingResult)
          throws AppException {
    if (bindingResult.hasErrors())
      throw new ValidateException(bindingResult.getFieldError().getDefaultMessage());
    submissionDTO.setProblemId(id);
    return submissionService.createPracticeSubmission(submissionDTO);
  }

  @PutMapping(value = "/submissions/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public SubmissionDTO rejudgeSubmissions(@PathVariable String id, @RequestParam(required = false) Result result) throws AppException {
    return submissionService.rejudgeSubmission(id, result);
  }

}
