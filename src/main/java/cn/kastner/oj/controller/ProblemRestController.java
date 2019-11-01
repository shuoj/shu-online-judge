package cn.kastner.oj.controller;

import cn.kastner.oj.dto.PageDTO;
import cn.kastner.oj.dto.ProblemDTO;
import cn.kastner.oj.exception.*;
import cn.kastner.oj.query.ProblemQuery;
import cn.kastner.oj.service.ProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/problems")
public class ProblemRestController {

  private final ProblemService problemService;

  @Autowired
  public ProblemRestController(ProblemService problemService) {
    this.problemService = problemService;
  }

  /**
   * 获取指定题目
   *
   * @param id 问题id
   * @throws NoSuchItemException 没有这个问题
   */
  @GetMapping(value = "/{id}")
  public ProblemDTO getProblem(@PathVariable String id) throws AppException {
    return problemService.findProblemById(id);
  }

  /**
   * 多参数查询题目
   *
   * @param page 页码 默认 0
   * @param size 每页数量 默认 10
   */
  @GetMapping
  public PageDTO<ProblemDTO> getProblems(
      ProblemQuery problemQuery,
      @RequestParam(value = "page", defaultValue = "0") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size) {
    return problemService.findProblemCriteria(page, size, problemQuery);
  }

  /**
   * 创建题目
   *
   * @throws ValidateException 无权限
   */
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public ProblemDTO createProblem(
      @Validated @RequestBody ProblemDTO problemDTO, BindingResult bindingResult)
      throws AppException {
    if (bindingResult.hasErrors()) {
      throw new ValidateException(bindingResult.getFieldError().getDefaultMessage());
    } else {
      return problemService.create(problemDTO);
    }
  }

  /**
   * 更新题目信息
   *
   * @param problemDTO { 和创建题目所需参数相同 }
   * @param id         题目id
   * @return { 更新后的题目信息 }
   * @throws NoSuchItemException   没有此用户
   * @throws ValidateException     无权限
   * @throws HaveSuchItemException 已经有相同title的题目了
   */
  @PutMapping(value = "/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public ProblemDTO updateProblem(
      @Validated @RequestBody ProblemDTO problemDTO,
      BindingResult bindingResult,
      @PathVariable String id)
      throws AppException {
    if (bindingResult.hasErrors()) {
      throw new ValidateException(bindingResult.getFieldError().getDefaultMessage());
    } else {
      problemDTO.setId(id);
      return problemService.update(problemDTO);
    }
  }

  /**
   * 删除题目
   *
   * @param id 题目id
   * @return { 删除的题目 }
   * @throws NoSuchItemException 没有这个题目
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUFF')")
  public ProblemDTO deleteProblem(@PathVariable String id) throws ProblemException {
    return problemService.delete(id);
  }
}
